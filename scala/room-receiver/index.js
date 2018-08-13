Index.PACKAGES = {"it" : [], "it.cwmp" : [], "it.cwmp.services" : [], "it.cwmp.services.roomreceiver" : [{"name" : "it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle", "shortDescription" : "A class implementing a one-time service provided by clients to receive room information", "members_case class" : [{"label" : "RichHttpRequest", "tail" : "", "member" : "it.cwmp.services.VertxServer.RichHttpRequest", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#RichHttpRequestextendsAnyRef", "kind" : "implicit class"}, {"label" : "initRouter", "tail" : "(router: Router): Unit", "member" : "it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle.initRouter", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#initRouter(router:io.vertx.scala.ext.web.Router):Unit", "kind" : "def"}, {"label" : "port", "tail" : "(): Int", "member" : "it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle.port", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#port:Int", "kind" : "def"}, {"label" : "serverPort", "tail" : ": Int", "member" : "it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle.serverPort", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#serverPort:Int", "kind" : "val"}, {"member" : "it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle#<init>", "error" : "unsupported entity"}, {"label" : "receptionStrategy", "tail" : ": (List[Participant]) ⇒ Unit", "member" : "it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle.receptionStrategy", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#receptionStrategy:List[it.cwmp.model.Participant]=>Unit", "kind" : "val"}, {"label" : "token", "tail" : ": String", "member" : "it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle.token", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#token:String", "kind" : "val"}, {"label" : "log", "tail" : ": Logger", "member" : "it.cwmp.utils.Logging.log", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#log:com.typesafe.scalalogging.Logger", "kind" : "implicit val"}, {"label" : "request", "tail" : "(routingContext: RoutingContext): HttpServerRequest", "member" : "it.cwmp.services.VertxServer.request", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#request(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):io.vertx.scala.core.http.HttpServerRequest", "kind" : "def"}, {"label" : "getRequestParameter", "tail" : "(paramName: String)(routingContext: RoutingContext): Option[String]", "member" : "it.cwmp.services.VertxServer.getRequestParameter", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#getRequestParameter(paramName:String)(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):Option[String]", "kind" : "def"}, {"label" : "response", "tail" : "(routingContext: RoutingContext): HttpServerResponse", "member" : "it.cwmp.services.VertxServer.response", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#response(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):io.vertx.scala.core.http.HttpServerResponse", "kind" : "def"}, {"label" : "sendResponse", "tail" : "(httpCode: Int, message: Option[String])(routingContext: RoutingContext): Unit", "member" : "it.cwmp.services.VertxServer.sendResponse", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#sendResponse(httpCode:Int,message:Option[String])(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):Unit", "kind" : "def"}, {"label" : "initServer", "tail" : "(): Future[_]", "member" : "it.cwmp.services.VertxServer.initServer", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#initServer:scala.concurrent.Future[_]", "kind" : "def"}, {"label" : "startFuture", "tail" : "(): Future[_]", "member" : "it.cwmp.services.VertxServer.startFuture", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#startFuture():scala.concurrent.Future[_]", "kind" : "def"}, {"label" : "server", "tail" : ": HttpServer", "member" : "it.cwmp.services.VertxServer.server", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#server:io.vertx.scala.core.http.HttpServer", "kind" : "val"}, {"label" : "asJava", "tail" : "(): Verticle", "member" : "io.vertx.lang.scala.ScalaVerticle.asJava", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#asJava():io.vertx.core.Verticle", "kind" : "def"}, {"label" : "processArgs", "tail" : "(): Buffer[String]", "member" : "io.vertx.lang.scala.ScalaVerticle.processArgs", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#processArgs:scala.collection.mutable.Buffer[String]", "kind" : "def"}, {"label" : "config", "tail" : "(): JsonObject", "member" : "io.vertx.lang.scala.ScalaVerticle.config", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#config:io.vertx.core.json.JsonObject", "kind" : "def"}, {"label" : "deploymentID", "tail" : "(): String", "member" : "io.vertx.lang.scala.ScalaVerticle.deploymentID", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#deploymentID:String", "kind" : "def"}, {"label" : "stopFuture", "tail" : "(): Future[_]", "member" : "io.vertx.lang.scala.ScalaVerticle.stopFuture", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#stopFuture():scala.concurrent.Future[_]", "kind" : "def"}, {"label" : "stop", "tail" : "(): Unit", "member" : "io.vertx.lang.scala.ScalaVerticle.stop", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#stop():Unit", "kind" : "def"}, {"label" : "start", "tail" : "(): Unit", "member" : "io.vertx.lang.scala.ScalaVerticle.start", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#start():Unit", "kind" : "def"}, {"label" : "init", "tail" : "(vertx: Vertx, context: Context, verticle: AbstractVerticle): Unit", "member" : "io.vertx.lang.scala.ScalaVerticle.init", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#init(vertx:io.vertx.core.Vertx,context:io.vertx.core.Context,verticle:io.vertx.core.AbstractVerticle):Unit", "kind" : "def"}, {"label" : "ctx", "tail" : ": Context", "member" : "io.vertx.lang.scala.ScalaVerticle.ctx", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#ctx:io.vertx.scala.core.Context", "kind" : "var"}, {"label" : "vertx", "tail" : ": Vertx", "member" : "io.vertx.lang.scala.ScalaVerticle.vertx", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#vertx:io.vertx.scala.core.Vertx", "kind" : "var"}, {"label" : "executionContext", "tail" : ": VertxExecutionContext", "member" : "io.vertx.lang.scala.ScalaVerticle.executionContext", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#executionContext:io.vertx.lang.scala.VertxExecutionContext", "kind" : "implicit var"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#notify():Unit", "kind" : "final def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#clone():Object", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "case class" : "it\/cwmp\/services\/roomreceiver\/RoomReceiverServiceVerticle.html", "kind" : "case class"}]};