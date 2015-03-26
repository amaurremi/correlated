package ca.uwaterloo.dataflow.ide.taint

import ca.uwaterloo.dataflow.common.Time.time
import ca.uwaterloo.dataflow.ifds.instance.taint.impl.{SecretInput, SecretStrings}
import org.scalatest.FunSpec

class TaintAnalysisSpec extends FunSpec {

  private[this] def assertSecretsFor(test: String, useSecretStrings: Boolean = true) {
    val dir = "ca/uwaterloo/dataflow/ide/taint/"
    val path = dir + test
    lazy val (ifds, ide) =
      if (useSecretStrings)
        (new TaintAnalysisSpecBuilder(path) with SecretStrings, new CcTaintAnalysisSpecBuilder(path) with SecretStrings)
      else
        (new TaintAnalysisSpecBuilder(path) with SecretInput, new CcTaintAnalysisSpecBuilder(path) with SecretStrings)
    println(test + " unit test...")
    time("preparing IFDS analysis...") {
      ifds
    }.assertSecretValues()
    time("preparing IDE analysis...") {
      ide
    }.assertSecretValues()
  }

  describe("IFDS and correlated-calls taint analyses") {

    describe("general") {
      it("propagates secret values intra-procedurally") {
        assertSecretsFor("LocalVars")
      }

      it("propagates non-secret values intra-procedurally") {
        assertSecretsFor("NotSecretLocalVars")
      }

      it("propagates secret values along the call-start edge (instance method, multiple parameters, multiple files)") {
        assertSecretsFor("MultipleFiles")
      }

      it("propagates secret values along the end-return edge") {
        assertSecretsFor("ReturnSecret")
      }

      it("propagates secret-value-storing variables along the end-return edge") {
        assertSecretsFor("Return")
      }
    }

    describe("exceptions") {
      it("propagates secret values in try/catch blocks") {
        assertSecretsFor("TryCatch")
      }

      // Ignoring fields
      /*it("propagates secret values via exception objects") {
        assertSecretsFor("TryCatch2")
      }*/
    }

    describe("function calls") {
      it("propagates secret values along the call-start edge (static method)") {
        assertSecretsFor("FunctionCall")
      }

      it("propagates secret values along the call-start edge (instance method)") {
        assertSecretsFor("FunctionCall2")
      }

      it("propagates secret values along the call-start edge (instance method, multiple parameters)") {
        assertSecretsFor("FunctionCall3")
      }

      it("propagates secret values along the return edge (static method)") {
        assertSecretsFor("FunctionReturn")
      }

      it("propagates secret values along the return edge (static method) 2") {
        assertSecretsFor("FunctionReturn2")
      }

      it("propagates secret values along the return edge (static method) 3") {
        assertSecretsFor("FunctionReturn3")
      }

      it("propagates secret values along the return edge (static method) 4") {
        assertSecretsFor("FunctionReturn4")
      }

      it("propagates secret values along the return edge (instance method) 5") {
        assertSecretsFor("FunctionReturn5")
      }

      it("propagates secret values along the return edge (instance method) 6") {
        assertSecretsFor("FunctionReturn6")
      }

      it("propagates secret values along the return edge (instance method) 7") {
        assertSecretsFor("FunctionReturn7")
      }

      it("propagates secret values along the return edge (instance method) 8") {
        assertSecretsFor("FunctionReturn8")
      }

      it("propagates secret values along the return edge (static method) 9") {
        assertSecretsFor("FunctionReturn9")
      }

      it("sets a function parameter to top, if that function is invoked with secret and non-secret arguments") {
        assertSecretsFor("MultipleFunctionCalls")
      }
    }

    describe("control flow") {
      it("propagates the code following a constructor") {
        assertSecretsFor("ReturnFromConstructor")
      }
    }

    describe("recursion") {
      it("recursive call") {
        assertSecretsFor("Recursion")
      }

      it("recursive call 2") {
        assertSecretsFor("Recursion2")
      }

      it("recursive call 3") {
        assertSecretsFor("Recursion3")
      }

      it("recursive call 4") {
        assertSecretsFor("Recursion4")
      }

      it("recursive call 5") {
        assertSecretsFor("Recursion5")
      }

      it("recursive call 6") {
        assertSecretsFor("Recursion6")
      }

      it("recursive call 7") {
        assertSecretsFor("Recursion7")
      }

      it("recursive call 8") {
        assertSecretsFor("Recursion8")
      }
    }

    describe("conditionals") {
      it("assigns top to variables that have been assigned secret and non-secret values in if branches") {
        assertSecretsFor("Phi")
      }

      it("propagate constant variables that have been assigned the same value in different if branches") {
        assertSecretsFor("PhiSame")
      }

      it("switch statement") {
        assertSecretsFor("Switch")
      }

      it("switch statement 2") {
        assertSecretsFor("Switch2")
      }

      it("switch statement 3") {
        assertSecretsFor("Switch3")
      }

      it("if statement") {
        assertSecretsFor("If")
      }

      it("if statement 2") {
        assertSecretsFor("If2")
      }

      it("if statement 3") {
        assertSecretsFor("If3")
      }

      it("if statement 4") {
        assertSecretsFor("If4")
      }

      it("nested if statement") {
        assertSecretsFor("NestedIf")
      }

      it("nested if statement 2") {
        assertSecretsFor("NestedIf2")
      }

      it("ternary operator") {
        assertSecretsFor("Ternary")
      }
    }

    describe("casting and instanceof") {
      it("cast") {
        assertSecretsFor("Cast")
      }

      it("cast2") {
        assertSecretsFor("Cast2")
      }

      it("cast3") {
        assertSecretsFor("Cast3")
      }

      it("cast4") {
        assertSecretsFor("Cast4")
      }

      it("cast5") {
        assertSecretsFor("Cast5")
      }

      it("cast6") {
        assertSecretsFor("Cast6")
      }

      it("instanceof") {
        assertSecretsFor("Instanceof")
      }

      it("instanceof2") {
        assertSecretsFor("Instanceof2")
      }
    }

    describe("arrays") {
      it("array") {
        assertSecretsFor("Array")
      }

      it("array2") {
        assertSecretsFor("Array2")
      }

      it("array3") {
        assertSecretsFor("Array3")
      }

      it("array4") {
        assertSecretsFor("Array4")
      }

      it("array5") {
        assertSecretsFor("Array5")
      }

      it("array6") {
        assertSecretsFor("Array6")
      }

      it("array7") {
        assertSecretsFor("Array7")
      }

      it("array element passed as parameter") {
        assertSecretsFor("ArrayElementAsParameter")
      }

      it("main args are secret") {
        assertSecretsFor("MainArgsArray")
      }
    }

    // Disabling fields test for ignore-fields branch
/*    describe("fields") {
      it("field") {
        assertSecretsFor("Field")
      }

      it("field2") {
        assertSecretsFor("Field2")
      }

      it("field3") {
        assertSecretsFor("Field3")
      }

      it("field4") {
        assertSecretsFor("Field4")
      }

      it("field5") {
        assertSecretsFor("Field5")
      }

      it("field6") {
        assertSecretsFor("Field6")
      }

      it("field7") {
        assertSecretsFor("Field7")
      }

      it("static initialized field") {
        assertSecretsFor("Field8")
      }

      it("instance field initialized") {
        assertSecretsFor("Field9")
      }

      it("static initialized fields") {
        assertSecretsFor("Field10")
      }

      it("field passed as parameter") {
        assertSecretsFor("FieldAsParameter")
      }
    }*/

    describe("inheritance") {
      it("inheritance") {
        assertSecretsFor("Inheritance")
      }

      it("inheritance2") {
        assertSecretsFor("Inheritance2")
      }

      it("inheritance3") {
        assertSecretsFor("Inheritance3")
      }

      it("inheritance4") {
        assertSecretsFor("Inheritance4")
      }

      it("inheritance5") {
        assertSecretsFor("Inheritance5")
      }

      it("inheritance6") {
        assertSecretsFor("Inheritance6")
      }

      it("inheritance7") {
        assertSecretsFor("Inheritance7")
      }

      it("inheritance8") {
        assertSecretsFor("Inheritance8")
      }

      it("inheritance9") {
        assertSecretsFor("Inheritance9")
      }

      it("inheritance10") {
        assertSecretsFor("Inheritance10")
      }

      it("inheritance11") {
        assertSecretsFor("Inheritance11")
      }

      it("inheritance12") {
        assertSecretsFor("Inheritance12")
      }

      it("overloading") {
        assertSecretsFor("Overloading")
      }
    }

    describe("loops") {
      it("for statement") {
        assertSecretsFor("For")
      }

      it("for statement 2") {
        assertSecretsFor("For2")
      }

      it("for statement 3") {
        assertSecretsFor("For3")
      }

      it("for statement 4") {
        assertSecretsFor("For4")
      }

      it("while statement") {
        assertSecretsFor("While")
      }

      it("while statement 2") {
        assertSecretsFor("While2")
      }

      it("do statement") {
        assertSecretsFor("Do")
      }

      it("do statement 2") {
        assertSecretsFor("Do2")
      }
    }

    describe("generics") {
      it("generics") {
        assertSecretsFor("Generics")
      }

      it("generics2") {
        assertSecretsFor("Generics2")
      }
    }

    describe("string operations and concatenation") {
      it("StringBuffer") {
        assertSecretsFor("StringBuffer")
      }

      it("StringBuilder phi instructions and assignments") {
        assertSecretsFor("StringBuilderPhi")
      }

      it("string concatenation") {
        assertSecretsFor("StringConcat")
      }

      it("string concatenation2") {
        assertSecretsFor("StringConcat2")
      }

      it("string operations") {
        assertSecretsFor("StringOps")
      }

      it("string builder field assignment") {
        assertSecretsFor("StringBuilder1")
      }

      it("StringBuffer2") {
        assertSecretsFor("StringBuffer2")
      }

      it("StringBuffer3") {
        assertSecretsFor("StringBuffer3")
      }

      it("StringBuffer4") {
        assertSecretsFor("StringBuffer4")
      }

      it("StringBuffer5") {
        assertSecretsFor("StringBuffer5")
      }

      it("StringBuffer6") {
        assertSecretsFor("StringBuffer6")
      }

      it("StringBuffer7") {
        assertSecretsFor("StringBuffer7")
      }

      it("StringBuilder") {
        assertSecretsFor("StringBuilder")
      }

      it("StringBuilder1") {
        assertSecretsFor("StringBuilder1")
      }

      it("StringBuilder2") {
        assertSecretsFor("StringBuilder2")
      }

      it("StringBuilder3") {
        assertSecretsFor("StringBuilder3")
      }

      it("StringBuilder4") {
        assertSecretsFor("StringBuilder4")
      }

      it("StringBuilder5") {
        assertSecretsFor("StringBuilder5")
      }

      it("StringBuilder6") {
        assertSecretsFor("StringBuilder6")
      }

      it("StringBuilder7") {
        assertSecretsFor("StringBuilder7")
      }
    }

    describe("library calls") {
      it("library calls1") {
        assertSecretsFor("LibCalls")
      }
    }

    describe("correlated calls") {

      // ignoring fields
      /*it("correlated calls") {
        assertSecretsFor("Correlated1")
      }*/

      it("correlated calls 2") {
        assertSecretsFor("Correlated2")
      }

      it("correlated calls 3") {
        assertSecretsFor("Correlated3")
      }

      it("correlated calls 4") {
        assertSecretsFor("Correlated4")
      }

      it("correlated calls 5") {
        assertSecretsFor("Correlated5")
      }

      it("correlated calls 6") {
        assertSecretsFor("Correlated6")
      }

      it("correlated calls 7") {
        assertSecretsFor("Correlated7")
      }

      it("correlated calls 8") {
        assertSecretsFor("Correlated8")
      }

      it("correlated calls 9") {
        assertSecretsFor("Correlated9")
      }

      it("correlated calls 10") {
        assertSecretsFor("Correlated10")
      }
    }
  }

  describe("Taint analysis with primitive types") {
    it("propagates secret ints, chars, and Strings") {
      assertSecretsFor("Primitive")
    }
  }
}
