package ca.uwaterloo.dataflow.ide.taint

import org.scalatest.{BeforeAndAfterAll, FunSpec}

class TaintAnalysisSpec extends FunSpec with BeforeAndAfterAll {

  override def beforeAll() {
//    SpecUtil.rebuild("ide/taint", "taint")
  }

  private[this] def assertSecretsFor(test: String) {
    new TaintAnalysisSpecBuilder(test).assertSecretValues()
    new CcTaintAnalysisSpecBuilder(test).assertSecretValues()
  }
  
  describe("IfdsTaintAnalysis") {

    it("propagates secret values in try/catch blocks") {
      assertSecretsFor("TryCatch")
    }

    it("propagates secret values via exception objects") {
      assertSecretsFor("TryCatch2")
    }

    it("propagates secret values intra-procedurally") {
      assertSecretsFor("LocalVars")
    }

    it("propagates non-secret values intra-procedurally") {
      assertSecretsFor("NotSecretLocalVars")
    }

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

    it("propagates secret values along the call-start edge (instance method, multiple parameters, multiple files)") {
      assertSecretsFor("MultipleFiles")
    }

    it("sets a function parameter to top, if that function is invoked with secret and non-secret arguments") {
      assertSecretsFor("MultipleFunctionCalls")
    }

    it("propagates secret values along the end-return edge") {
      assertSecretsFor("ReturnSecret")
    }

    it("propagates secret-value-storing variables along the end-return edge") {
      assertSecretsFor("Return")
    }

    it("assigns top to variables that have been assigned secret and non-secret values in if branches") {
      assertSecretsFor("Phi")
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

    it("propagate constant variables that have been assigned the same value in different if branches") {
      assertSecretsFor("PhiSame")
    }

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

    it("StringBuffer") {
      assertSecretsFor("StringBuffer")
    }

    it("generics") {
      assertSecretsFor("Generics")
    }

    it("generics2") {
      assertSecretsFor("Generics2")
    }

    it("string concatenation") {
      assertSecretsFor("StringConcat")
    }

    it("string operations") {
      assertSecretsFor("StringOps")
    }

    // correlated calls tests

    it("correlated calls") {
      assertSecretsFor("Correlated1")
    }
  }
}
