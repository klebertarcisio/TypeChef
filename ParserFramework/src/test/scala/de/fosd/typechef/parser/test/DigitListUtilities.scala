package de.fosd.typechef.parser.test

import de.fosd.typechef.parser._
import junit.framework._;
import junit.framework.Assert._
import de.fosd.typechef.featureexpr._
import org.junit.Test

trait DigitListUtilities {
    val f1 = FeatureExpr.createDefinedExternal("a")
    val f2 = FeatureExpr.createDefinedExternal("b")
    val l1 = Lit(1)
    val l2 = Lit(2)
    val l3 = Lit(3)

    def t(text: String): MyToken = t(text, FeatureExpr.base)
    def t(text: String, feature: FeatureExpr): MyToken = new MyToken(text, feature)
    def outer(x: AST) = DigitList2(List(o(x)))
    def wrapList(x: AST*) = DigitList2(List() ++ x.map(Opt(FeatureExpr.base, _)))
    def wrapList(x: List[AST]) : DigitList2 = wrapList(x :_*)



    def o(ast: AST) = Opt(FeatureExpr.base, ast)
}