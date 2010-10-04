package de.fosd.typechef.parser.c

import de.fosd.typechef.featureexpr.FeatureExpr
import de.fosd.typechef.parser._

//Expressions
trait AST

abstract class Expr extends AST
abstract class PrimaryExpr extends Expr
case class Id(name: String) extends PrimaryExpr
case class Constant(value: String) extends PrimaryExpr
case class StringLit(name: List[String]) extends PrimaryExpr

abstract class PostfixSuffix extends AST
case class SimplePostfixSuffix(t: String) extends PostfixSuffix
case class PointerPostfixSuffix(kind: String, id: Id) extends PostfixSuffix
case class FunctionCall(params: ExprList) extends PostfixSuffix
case class ArrayAccess(expr: Expr) extends PostfixSuffix

case class PostfixExpr(p: Expr, s: List[PostfixSuffix]) extends Expr
case class UnaryExpr(kind: String, e: Expr) extends Expr
case class SizeOfExprT(typeName: TypeName) extends Expr
case class SizeOfExprU(expr: Expr) extends Expr
case class CastExpr(typeName: TypeName, expr: Expr) extends Expr
case class UCastExpr(kind: String, castExpr: Expr) extends Expr

case class NAryExpr(e: Expr, others: List[(String, Expr)]) extends Expr
case class ConditionalExpr(condition: Expr, thenExpr: Option[Expr], elseExpr: Expr) extends Expr
case class AssignExpr(target: Expr, operation: String, source: Expr) extends Expr
case class ExprList(exprs: List[Expr]) extends Expr

//Statements
abstract class Statement extends AST
case class CompoundStatement(decl: List[Opt[Declaration]], innerStatements: List[Opt[Statement]]) extends Statement
case class EmptyStatement() extends Statement
case class ExprStatement(expr: Expr) extends Statement
case class WhileStatement(expr: Expr, s: Statement) extends Statement
case class DoStatement(expr: Expr, s: Statement) extends Statement
case class ForStatement(expr1: Option[Expr], expr2: Option[Expr], expr3: Option[Expr], s: Statement) extends Statement
case class GotoStatement(target: Expr) extends Statement
case class ContinueStatement() extends Statement
case class BreakStatement() extends Statement
case class ReturnStatement(expr: Option[Expr]) extends Statement
case class LabelStatement(id: Id) extends Statement
case class CaseStatement(c: Expr, s: Option[Statement]) extends Statement
case class DefaultStatement(s: Option[Statement]) extends Statement
case class IfStatement(condition: Expr, thenBranch: Statement, elseBranch: Option[Statement]) extends Statement
case class SwitchStatement(expr: Expr, s: Statement) extends Statement
case class AltStatement(feature: FeatureExpr, thenBranch: Statement, elseBranch: Statement) extends Statement
object AltStatement {
    def join = (f: FeatureExpr, x: Statement, y: Statement) => if (x == y) x else AltStatement(f, x, y)
}

abstract class Specifier extends AST
abstract class TypeSpecifier extends Specifier
case class PrimitiveTypeSpecifier(typeName: String) extends TypeSpecifier
case class TypeDefTypeSpecifier(name: Id) extends TypeSpecifier
case class TypedefSpecifier() extends Specifier
case class OtherSpecifier(name: String) extends Specifier

abstract class Attribute extends AST
case class AtomicAttribute(n: String) extends Attribute
case class CompoundAttribute(inner: List[List[Attribute]]) extends Attribute

trait Declaration extends AST with ExternalDef
case class ADeclaration(declSpecs: List[Specifier], init: Option[List[InitDeclarator]]) extends Declaration
case class AltDeclaration(feature: FeatureExpr, thenBranch: Declaration, elseBranch: Declaration) extends Declaration
object AltDeclaration {
    def join = (f: FeatureExpr, x: Declaration, y: Declaration) => if (x == y) x else AltDeclaration(f, x, y)
}

abstract class InitDeclarator(val declarator: Declarator, val attributes: List[Specifier]) extends AST
case class InitDeclaratorI(override val declarator: Declarator, override val attributes: List[Specifier], i: Option[Initializer]) extends InitDeclarator(declarator, attributes)
case class InitDeclaratorE(override val declarator: Declarator, override val attributes: List[Specifier], e: Expr) extends InitDeclarator(declarator, attributes)

abstract class Declarator(pointer: List[Pointer], extensions: List[DeclaratorExtension]) extends AST {
    def getName: String
}
case class DeclaratorId(pointer: List[Pointer], id: Id, extensions: List[DeclaratorExtension]) extends Declarator(pointer, extensions) {
    def getName = id.name
}
case class DeclaratorDecl(pointer: List[Pointer], decl: Declarator, extensions: List[DeclaratorExtension]) extends Declarator(pointer, extensions) {
    def getName = decl.getName
}
abstract class DeclaratorExtension extends AST
case class DeclIdentifierList(idList: List[Id]) extends DeclaratorExtension
case class DeclParameterTypeList(parameterTypes: List[ParameterDeclaration]) extends DeclaratorExtension with DirectAbstractDeclarator
case class DeclArrayAccess(expr: Option[Expr]) extends DeclaratorExtension with DirectAbstractDeclarator

trait DirectAbstractDeclarator
case class AbstractDeclarator(pointer: List[Pointer], extensions: List[DirectAbstractDeclarator]) extends AST with DirectAbstractDeclarator

case class Initializer(initializerElementLabel: Option[InitializerElementLabel], expr: Expr) extends AST

case class Pointer(specifier: List[Specifier])
class ParameterDeclaration(val specifiers: List[Specifier]) extends AST
case class PlainParameterDeclaration(override val specifiers: List[Specifier]) extends ParameterDeclaration(specifiers)
case class ParameterDeclarationD(override val specifiers: List[Specifier], decl: Declarator) extends ParameterDeclaration(specifiers)
case class ParameterDeclarationAD(override val specifiers: List[Specifier], decl: AbstractDeclarator) extends ParameterDeclaration(specifiers)
case class VarArgs() extends ParameterDeclaration(List()) with Declaration

case class EnumSpecifier(id: Option[Id], enumerators: List[Enumerator]) extends TypeSpecifier
case class Enumerator(id: Id, assignment: Option[Expr]) extends AST
case class StructOrUnionSpecifier(kind: String, id: Option[Id], enumerators: List[StructDeclaration]) extends TypeSpecifier
case class StructDeclaration(qualifierList: List[Specifier], declaratorList: List[StructDeclarator]) extends AST
case class StructDeclarator(declarator: Option[Declarator], expr: Option[Expr], attributes: List[Specifier]) extends AST

case class AsmExpr(isVolatile: Boolean, expr: Expr) extends AST with ExternalDef

case class FunctionDef(specifiers: List[Specifier], declarator: Declarator, parameters: List[Declaration], stmt: Statement) extends AST with ExternalDef
trait ExternalDef extends AST
case class EmptyExternalDef() extends ExternalDef
case class TypelessDeclaration(declList: List[InitDeclarator]) extends ExternalDef
case class AltExternalDef(feature: FeatureExpr, thenBranch: ExternalDef, elseBranch: ExternalDef) extends ExternalDef
object AltExternalDef {
    def join = (f: FeatureExpr, x: ExternalDef, y: ExternalDef) => if (x == y) x else AltExternalDef(f, x, y)
}

case class TypeName(specifiers: List[Specifier], decl: Option[AbstractDeclarator]) extends AST

//GnuC stuff here:
case class AttributeSpecifier(attributeList: List[List[Attribute]]) extends Specifier
case class AsmAttributeSpecifier(stringConst: StringLit) extends Specifier
case class LcurlyInitializer(inits: List[Initializer]) extends Expr
case class AlignOfExprT(typeName: TypeName) extends Expr
case class AlignOfExprU(expr: Expr) extends Expr
case class GnuAsmExpr(isVolatile: Boolean, expr: StringLit, stuff: Any) extends Expr
case class RangeExpr(from: Expr, to: Expr) extends Expr
case class NestedFunctionDef(isAuto: Boolean, specifiers: List[Specifier], declarator: Declarator, parameters: List[Declaration], stmt: Statement) extends Declaration
case class TypeOfSpecifierT(typeName: TypeName) extends TypeSpecifier
case class TypeOfSpecifierU(expr: Expr) extends TypeSpecifier
case class LocalLabelDeclaration(ids: List[Id]) extends Declaration
abstract class InitializerElementLabel() extends AST
case class InitializerElementLabelExpr(expr: Expr, isAssign: Boolean) extends InitializerElementLabel
case class InitializerElementLabelColon(id: Id) extends InitializerElementLabel
case class InitializerElementLabelDotAssign(id: Id) extends InitializerElementLabel
case class BuildinOffsetof(typeName: TypeName, offsetofMemberDesignator: List[Id]) extends PrimaryExpr
case class CompoundStatementExpr(compoundStatement: CompoundStatement) extends PrimaryExpr

