import play.sbt.routes.RoutesKeys.routes
import sbt._
import sbt.Keys._
import wartremover.Wart
import wartremover.WartRemover.autoImport.{Warts, wartremoverErrors, wartremoverExcluded}

object WartRemoverSettings {

  lazy val wartRemoverSettings =
    Seq(
      (Compile / compile / wartremoverErrors) ++= Warts.allBut(
        Wart.DefaultArguments,
        Wart.ImplicitConversion,
        Wart.ImplicitParameter,
        Wart.Nothing,
        Wart.Overloading,
        Wart.SizeIs,
        Wart.SortedMaxMinOption,
        Wart.Throw,
        Wart.ToString,
      ),
      Test / compile / wartremoverErrors --= Seq(
        Wart.Any,
        Wart.Equals,
        Wart.GlobalExecutionContext,
        Wart.Null,
        Wart.NonUnitStatements,
        Wart.PublicInference
      ),
      IntegrationTest / compile / wartremoverErrors --= Seq(),
      wartremoverExcluded ++= (
        (baseDirectory.value ** "*.sc").get ++
        (Compile / routes).value
      ),
      Compile / doc / wartremoverErrors := Seq()
    )

}

//
//object  WartRemoverSettings {
//
//  lazy val wartRemoverWarning = {
//    val warningWarts = Seq(
//      Wart.JavaSerializable,
//      Wart.StringPlusAny,
//      Wart.AsInstanceOf,
//      Wart.IsInstanceOf,
//      Wart.Any
//    )
//    Compile /  compile / wartremoverWarnings ++= warningWarts
//  }
//  lazy val wartRemoverError = {
//    // Error
//    val errorWarts = Seq(
//      Wart.ArrayEquals,
//      Wart.AnyVal,
//      Wart.EitherProjectionPartial,
//      Wart.Enumeration,
//      Wart.ExplicitImplicitTypes,
//      Wart.FinalVal,
//      Wart.JavaConversions,
//      Wart.JavaSerializable,
//      Wart.LeakingSealed,
//      Wart.MutableDataStructures,
//      Wart.Null,
//      Wart.OptionPartial,
//      Wart.Recursion,
//      Wart.Return,
//      Wart.TryPartial,
//      Wart.Var,
//      Wart.While)
//
//    Compile / compile / wartremoverErrors ++= errorWarts
//  }
//}
