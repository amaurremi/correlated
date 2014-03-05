package ca.uwaterloo.ide.example.cp

import ca.uwaterloo.ide.{IdeFunctions, IdeProblem}
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.{AnalysisCache, CallGraph, CGNode}
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._

class ConstantPropagationProblem(fileName: String) extends IdeProblem {

  implicit val config =
    ConfigFactory.load(
      fileName,
      ConfigParseOptions.defaults().setAllowMissing(false),
      ConfigResolveOptions.defaults()
    )
  private[this] val fcgb = FlexibleCallGraphBuilder()
  private[this] val callGraph: CallGraph = fcgb.cg

  override type Node = BasicBlockInContext[IExplodedBasicBlock]
  override type Procedure = CGNode
  override type IdeFunction = CpFunction
  override type Fact = Int
  override type LatticeElem = CpLatticeElem

  override val intToFact: Int => Fact = identity
  override val factToInt: Fact => Int = identity
  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem = ⊤
  override val Λ: Fact = 0
  override val Id: IdeFunction = CpFunction(1, 0, ⊤)
  override val λTop: IdeFunction = CpFunction(1, 0, ⊤) // todo correct?

  override val entryPoints: Seq[Node] = callGraph.getEntrypointNodes.asScala.toSeq map ???
  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, fcgb._cache)

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn = ???

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: EdgeFn = ???

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: EdgeFn = ???

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartFns: EdgeFn = ???

  /**
   * Represents a function
   * λl . (a * l + b) ⊓ c
   * as described on p. 153 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
   * with applications to constant propagation"
   */
  case class CpFunction(a: Long, b: Long, c: LatticeElem) extends IdeFunctions[LatticeElem, CpFunction] {

    override def apply(arg: LatticeElem): LatticeElem = (Num(a) * arg + Num(b)) ⊓ c

    /**
     * Meet operator
     */
    override def ⊓(f: CpFunction): CpFunction =
      f match {
        case CpFunction(a2, b2, c2) =>
          if (a == a2 && b == b2)
            CpFunction(a, b, c ⊓ c2)
          else if (equiv(a2, b2, c2))
            this
          else
            CpFunction(1, 0, ⊥)
      }

    private def equiv(a2: Long, b2: Long, c2: LatticeElem): Boolean = {
      val l: Double = (b - b2) / (a2 - a)
      l.isWhole() && c == (Num(a * l.toInt + b) ⊓ c ⊓ c2)
    }

    override def ◦(f: CpFunction): CpFunction =
      f match {
        case CpFunction(a2, b2, c2) =>
          CpFunction(a * a2, a * b2 + b, (Num(a) * c2 + Num(b)) ⊓ c)
      }

    override def equals(obj: Any): Boolean =
      obj match {
        case CpFunction(_, _, ⊥) if c == ⊥ => true
        case _ => super.equals(obj)
      }
  }

  trait CpLatticeElem extends Lattice[CpLatticeElem] {
    def +(n:CpLatticeElem): CpLatticeElem
    def -(n: CpLatticeElem): CpLatticeElem
    def *(n: CpLatticeElem): CpLatticeElem
    def ⊓(n: CpLatticeElem): CpLatticeElem
    val inverse: CpLatticeElem
  }

  case object ⊤ extends CpLatticeElem {
    override def +(n: CpLatticeElem) = ⊤
    override def -(n: CpLatticeElem) = ⊤
    override def *(n: CpLatticeElem) = ⊤
    override def ⊓(n: CpLatticeElem) = n
    override val inverse = ⊤
  }

  case object ⊥ extends CpLatticeElem {
    override def +(n: CpLatticeElem) = ⊥
    override def -(n: CpLatticeElem) = ⊥
    override def *(n: CpLatticeElem) = ⊥
    override def ⊓(n: CpLatticeElem) = ⊥
    override val inverse = ⊥
  }

  case class Num(n: Long) extends CpLatticeElem {

    override def +(ln: CpLatticeElem) = ln match {
      case Num(n2) => Num(n + n2)
      case tb      => tb + this
    }

    override def -(ln: CpLatticeElem) = this + ln.inverse

    override def *(ln: CpLatticeElem) = ln match {
      case Num(n2) => Num(n * n2)
      case _       => ln * this
    }

    override val inverse: CpLatticeElem = Num(-n)

    override def ⊓(ln: CpLatticeElem) = ln match {
      case Num(n2) => if (n == n2) ln else ⊥
      case _       => ln ⊓ this
    }
  }
}
