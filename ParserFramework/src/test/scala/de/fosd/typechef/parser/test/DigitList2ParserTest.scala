package de.fosd.typechef.parser.test

import de.fosd.typechef.parser._
import junit.framework._;
import junit.framework.Assert._
import de.fosd.typechef.featureexpr._
import org.junit.Test

class DigitList2ParserTest extends TestCase with DigitListUtilities {
    val newParser = new DigitList2Parser() {
        type OptResult[T] = Opt[T]
        def myRepOpt[T](p: => MultiParser[T], joinFunction: (FeatureExpr, T, T) => T, productionName: String): MultiParser[List[OptResult[T]]] =
            repOpt(p, "")
        def digits: MultiParser[AST] =
            myRepOpt(digitList | digit, Alt.join, "digitList") ^^! (Alt.join, { //List(Opt(AST)) -> DigitList[List[Opt[Lit]]
                DigitList2(_)
            })
    }


    def testError1() {
        val input = List(t("("), t("3", f1),t(")", f1.not), t(")"))
        val actual = newParser.parse(input)
        System.out.println(actual)
        actual match {
            case newParser.Success(ast, unparsed) => {
                fail("should not parse " + input + " but result was " + actual)
            }
            case newParser.NoSuccess(msg, context, unparsed, inner) =>

        }
    }

    @Test
    def testParseSimpleList() {
        {
            val input = List(t("("), t(")"))
            val expected = DigitList2(List())
            assertParseResult(expected, newParser.parse(input))
        }
        {
            val input = List(t("("), t("1"), t(")"))
            val expected = DigitList2(List(o(Lit(1))))
            assertParseResult(expected, newParser.parse(input))
        }
        {
            val input = List(t("("), t("1"), t("2"), t(")"))
            val expected = DigitList2(List(o(Lit(1)), o(Lit(2))))
            assertParseResult(expected, newParser.parse(input))
        }
    }

    @Test
    def testParseOptSimpleList1() {
        val input = List(t("("), t("1", f1), t("2", f1.not), t(")"))
        val expected = DigitList2(List(Opt(f1, Lit(1)),Opt(f1.not, Lit(2))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListFirst() {
        val input = List(t("("), t("1", f1), t("1"), t("2"), t(")"))
        val expected = DigitList2(List(Opt(f1, Lit(1)), o(Lit(1)), o(Lit(2))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListLast() {
        val input = List(t("("), t("1"), t("2"), t("3", f1), t(")"))
        val expected = DigitList2(List(o(Lit(1)), o(Lit(2)), Opt(f1, Lit(3))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListMid() {
        val input = List(t("("), t("1"), t("2", f1), t("3"), t(")"))
        val expected = DigitList2(List(o(Lit(1)), Opt(f1, Lit(2)), o(Lit(3))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl1() {
        val input = List(t("("), t("1"), t("2", f1), t("3", f2), t(")"))
        val expected = DigitList2(List(o(Lit(1)), Opt(f1, Lit(2)), Opt(f2, Lit(3))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl2() {
        val input = List(t("("), t("1", f2), t("2", f1), t("3", f2), t(")"))
        val expected = DigitList2(List(Opt(f2, Lit(1)), Opt(f1, Lit(2)), Opt(f2, Lit(3))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl3() {
        val input = List(t("("), t("1", f2), t("2", f1), t("3", f2.not), t(")"))
        val expected = DigitList2(List(Opt(f2, Lit(1)), Opt(f1, Lit(2)), Opt(f2.not, Lit(3))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl4() {
        val input = List(t("("), t("1", f2), t("2", f2.not), t("3", f2.not), t(")"))
        val expected = DigitList2(List(Opt(f2, Lit(1)), Opt(f2.not, Lit(2)), Opt(f2.not, Lit(3))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseInterleaved1() {
        val input = List(t("("), t("("), t("1"), t("2"), t(")"), t("3"), t(")"))
        val expected = DigitList2(List(o(DigitList2(List(o(Lit(1)), o(Lit(2))))), o(Lit(3))))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseInterleaved2() {
        val input = List(t("("), t("(", f1), t("1"), t("2"), t(")", f1), t("3"), t(")"))
        val expected = Alt(f1, DigitList2(List(o(DigitList2(List(Opt(f1, Lit(1)), Opt(f1, Lit(2))))), o(Lit(3)))), DigitList2(List(o(Lit(1)), o(Lit(2)), o(Lit(3)))))
        assertParseResult(expected, newParser.parse(input))
    }

    def testNoBacktrace {
        val input = List(t("1"), t("("))
        val expected = Lit(1)
        var actual = newParser.parse(input)
        println(actual)
        actual match {
            case newParser.Success(ast, unparsed) => fail("expected error, found " + ast + " - " + unparsed)
            case newParser.NoSuccess(msg, context, unparsed, inner) =>
        }
    }

    def assertParseResult(expected: AST, actual: newParser.ParseResult[AST]) {
        System.out.println(actual)
        actual match {
            case newParser.Success(ast, unparsed) => {
                assertTrue("parser did not reach end of token stream: " + unparsed, unparsed.atEnd)
                assertEquals("incorrect parse result", outer(expected), ast)
            }
            case newParser.NoSuccess(msg, context, unparsed, inner) =>
                fail(msg + " at " + unparsed + " with context " + context + " " + inner)
        }
    }    
}
