Index.PACKAGES = {"it" : [], "it.cwmp" : [], "it.cwmp.services" : [], "it.cwmp.services.rooms" : [{"name" : "it.cwmp.services.rooms.RoomDAO", "members_trait" : [{"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#toString():String", "kind" : "def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#clone():Object", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}, {"label" : "deleteAndRecreatePublicRoom", "tail" : "(playersNumber: Int): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomDAO.deleteAndRecreatePublicRoom", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#deleteAndRecreatePublicRoom(playersNumber:Int):scala.concurrent.Future[Unit]", "kind" : "abstract def"}, {"label" : "deleteRoom", "tail" : "(roomID: String): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomDAO.deleteRoom", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#deleteRoom(roomID:String):scala.concurrent.Future[Unit]", "kind" : "abstract def"}, {"label" : "exitPublicRoom", "tail" : "(playersNumber: Int)(user: User): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomDAO.exitPublicRoom", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#exitPublicRoom(playersNumber:Int)(implicituser:it.cwmp.model.User):scala.concurrent.Future[Unit]", "kind" : "abstract def"}, {"label" : "publicRoomInfo", "tail" : "(playersNumber: Int): Future[(Room, Seq[Address])]", "member" : "it.cwmp.services.rooms.RoomDAO.publicRoomInfo", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#publicRoomInfo(playersNumber:Int):scala.concurrent.Future[(it.cwmp.model.Room,Seq[it.cwmp.model.Address])]", "kind" : "abstract def"}, {"label" : "enterPublicRoom", "tail" : "(playersNumber: Int)(user: Participant, notificationAddress: Address): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomDAO.enterPublicRoom", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#enterPublicRoom(playersNumber:Int)(implicituser:it.cwmp.model.Participant,implicitnotificationAddress:it.cwmp.model.Address):scala.concurrent.Future[Unit]", "kind" : "abstract def"}, {"label" : "listPublicRooms", "tail" : "(): Future[Seq[Room]]", "member" : "it.cwmp.services.rooms.RoomDAO.listPublicRooms", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#listPublicRooms():scala.concurrent.Future[Seq[it.cwmp.model.Room]]", "kind" : "abstract def"}, {"label" : "exitRoom", "tail" : "(roomID: String)(user: User): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomDAO.exitRoom", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#exitRoom(roomID:String)(implicituser:it.cwmp.model.User):scala.concurrent.Future[Unit]", "kind" : "abstract def"}, {"label" : "roomInfo", "tail" : "(roomID: String): Future[(Room, Seq[Address])]", "member" : "it.cwmp.services.rooms.RoomDAO.roomInfo", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#roomInfo(roomID:String):scala.concurrent.Future[(it.cwmp.model.Room,Seq[it.cwmp.model.Address])]", "kind" : "abstract def"}, {"label" : "enterRoom", "tail" : "(roomID: String)(user: Participant, notificationAddress: Address): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomDAO.enterRoom", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#enterRoom(roomID:String)(implicituser:it.cwmp.model.Participant,implicitnotificationAddress:it.cwmp.model.Address):scala.concurrent.Future[Unit]", "kind" : "abstract def"}, {"label" : "createRoom", "tail" : "(roomName: String, playersNumber: Int): Future[String]", "member" : "it.cwmp.services.rooms.RoomDAO.createRoom", "link" : "it\/cwmp\/services\/rooms\/RoomDAO.html#createRoom(roomName:String,playersNumber:Int):scala.concurrent.Future[String]", "kind" : "abstract def"}], "shortDescription" : "A trait that describes the Rooms Data Access Object", "trait" : "it\/cwmp\/services\/rooms\/RoomDAO.html", "kind" : "trait"}, {"name" : "it.cwmp.services.rooms.RoomsLocalDAO", "shortDescription" : "A wrapper to access a local Vertx storage for Rooms", "object" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html", "members_object" : [{"label" : "publicPrefix", "tail" : ": String", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.publicPrefix", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#publicPrefix:String", "kind" : "val"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#toString():String", "kind" : "def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#clone():Object", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO$.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "members_case class" : [{"label" : "RichFuture", "tail" : "", "member" : "it.cwmp.utils.VertxJDBC.RichFuture", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#RichFuture[F<:<?>]extendsAnyRef", "kind" : "implicit class"}, {"label" : "deleteAndRecreatePublicRoom", "tail" : "(playersNumber: Int): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.deleteAndRecreatePublicRoom", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#deleteAndRecreatePublicRoom(playersNumber:Int):scala.concurrent.Future[Unit]", "kind" : "def"}, {"label" : "deleteRoom", "tail" : "(roomID: String): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.deleteRoom", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#deleteRoom(roomID:String):scala.concurrent.Future[Unit]", "kind" : "def"}, {"label" : "exitPublicRoom", "tail" : "(playersNumber: Int)(user: User): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.exitPublicRoom", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#exitPublicRoom(playersNumber:Int)(implicituser:it.cwmp.model.User):scala.concurrent.Future[Unit]", "kind" : "def"}, {"label" : "publicRoomInfo", "tail" : "(playersNumber: Int): Future[(Room, Seq[Address])]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.publicRoomInfo", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#publicRoomInfo(playersNumber:Int):scala.concurrent.Future[(it.cwmp.model.Room,Seq[it.cwmp.model.Address])]", "kind" : "def"}, {"label" : "enterPublicRoom", "tail" : "(playersNumber: Int)(user: Participant, address: Address): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.enterPublicRoom", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#enterPublicRoom(playersNumber:Int)(implicituser:it.cwmp.model.Participant,implicitaddress:it.cwmp.model.Address):scala.concurrent.Future[Unit]", "kind" : "def"}, {"label" : "listPublicRooms", "tail" : "(): Future[Seq[Room]]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.listPublicRooms", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#listPublicRooms():scala.concurrent.Future[Seq[it.cwmp.model.Room]]", "kind" : "def"}, {"label" : "exitRoom", "tail" : "(roomID: String)(user: User): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.exitRoom", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#exitRoom(roomID:String)(implicituser:it.cwmp.model.User):scala.concurrent.Future[Unit]", "kind" : "def"}, {"label" : "roomInfo", "tail" : "(roomID: String): Future[(Room, Seq[Address])]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.roomInfo", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#roomInfo(roomID:String):scala.concurrent.Future[(it.cwmp.model.Room,Seq[it.cwmp.model.Address])]", "kind" : "def"}, {"label" : "enterRoom", "tail" : "(roomID: String)(user: Participant, notificationAddress: Address): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.enterRoom", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#enterRoom(roomID:String)(implicituser:it.cwmp.model.Participant,implicitnotificationAddress:it.cwmp.model.Address):scala.concurrent.Future[Unit]", "kind" : "def"}, {"label" : "createRoom", "tail" : "(roomName: String, playersNumber: Int): Future[String]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.createRoom", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#createRoom(roomName:String,playersNumber:Int):scala.concurrent.Future[String]", "kind" : "def"}, {"label" : "initialize", "tail" : "(): Future[Unit]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.initialize", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#initialize():scala.concurrent.Future[Unit]", "kind" : "def"}, {"member" : "it.cwmp.services.rooms.RoomsLocalDAO#<init>", "error" : "unsupported entity"}, {"label" : "configurationPath", "tail" : ": Option[String]", "member" : "it.cwmp.services.rooms.RoomsLocalDAO.configurationPath", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#configurationPath:Option[String]", "kind" : "val"}, {"label" : "log", "tail" : ": Logger", "member" : "it.cwmp.utils.Logging.log", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#log:com.typesafe.scalalogging.Logger", "kind" : "implicit val"}, {"label" : "closeLastOpenedConnection", "tail" : "(): Unit", "member" : "it.cwmp.utils.VertxJDBC.closeLastOpenedConnection", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#closeLastOpenedConnection():Unit", "kind" : "def"}, {"label" : "closeAllConnections", "tail" : "(): Unit", "member" : "it.cwmp.utils.VertxJDBC.closeAllConnections", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#closeAllConnections():Unit", "kind" : "def"}, {"label" : "closeConnection", "tail" : "(connection: SQLConnection): Unit", "member" : "it.cwmp.utils.VertxJDBC.closeConnection", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#closeConnection(implicitconnection:io.vertx.scala.ext.sql.SQLConnection):Unit", "kind" : "def"}, {"label" : "openConnection", "tail" : "(): Future[SQLConnection]", "member" : "it.cwmp.utils.VertxJDBC.openConnection", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#openConnection():scala.concurrent.Future[io.vertx.scala.ext.sql.SQLConnection]", "kind" : "def"}, {"label" : "vertxExecutionContext", "tail" : ": VertxExecutionContext", "member" : "it.cwmp.utils.VertxInstance.vertxExecutionContext", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#vertxExecutionContext:io.vertx.lang.scala.VertxExecutionContext", "kind" : "implicit val"}, {"label" : "vertx", "tail" : ": Vertx", "member" : "it.cwmp.utils.VertxInstance.vertx", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#vertx:io.vertx.scala.core.Vertx", "kind" : "val"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#notify():Unit", "kind" : "final def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#clone():Object", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "case class" : "it\/cwmp\/services\/rooms\/RoomsLocalDAO.html", "kind" : "case class"}, {"name" : "it.cwmp.services.rooms.RoomsServiceMain", "shortDescription" : "Object that implements the Rooms micro-service entry-point", "object" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html", "members_object" : [{"label" : "log", "tail" : ": Logger", "member" : "it.cwmp.utils.Logging.log", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#log:com.typesafe.scalalogging.Logger", "kind" : "implicit val"}, {"label" : "vertxExecutionContext", "tail" : ": VertxExecutionContext", "member" : "it.cwmp.utils.VertxInstance.vertxExecutionContext", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#vertxExecutionContext:io.vertx.lang.scala.VertxExecutionContext", "kind" : "implicit val"}, {"label" : "vertx", "tail" : ": Vertx", "member" : "it.cwmp.utils.VertxInstance.vertx", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#vertx:io.vertx.scala.core.Vertx", "kind" : "val"}, {"label" : "main", "tail" : "(args: Array[String]): Unit", "member" : "scala.App.main", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#main(args:Array[String]):Unit", "kind" : "def"}, {"label" : "delayedInit", "tail" : "(body: ⇒ Unit): Unit", "member" : "scala.App.delayedInit", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#delayedInit(body:=>Unit):Unit", "kind" : "def"}, {"label" : "args", "tail" : "(): Array[String]", "member" : "scala.App.args", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#args:Array[String]", "kind" : "def"}, {"label" : "executionStart", "tail" : ": Long", "member" : "scala.App.executionStart", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#executionStart:Long", "kind" : "val"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#toString():String", "kind" : "def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#clone():Object", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceMain$.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "kind" : "object"}, {"name" : "it.cwmp.services.rooms.RoomsServiceVerticle", "shortDescription" : "Class that implements the Rooms micro-service", "object" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html", "members_class" : [{"label" : "RichHttpRequest", "tail" : "", "member" : "it.cwmp.utils.VertxServer.RichHttpRequest", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#RichHttpRequestextendsAnyRef", "kind" : "implicit class"}, {"label" : "initServer", "tail" : "(): Future[_]", "member" : "it.cwmp.services.rooms.RoomsServiceVerticle.initServer", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#initServer:scala.concurrent.Future[_]", "kind" : "def"}, {"label" : "initRouter", "tail" : "(router: Router): Unit", "member" : "it.cwmp.services.rooms.RoomsServiceVerticle.initRouter", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#initRouter(router:io.vertx.scala.ext.web.Router):Unit", "kind" : "def"}, {"label" : "serverPort", "tail" : ": Int", "member" : "it.cwmp.services.rooms.RoomsServiceVerticle.serverPort", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#serverPort:Int", "kind" : "val"}, {"member" : "it.cwmp.services.rooms.RoomsServiceVerticle#<init>", "error" : "unsupported entity"}, {"label" : "clientCommunicationStrategy", "tail" : ": RoomReceiverApiWrapper", "member" : "it.cwmp.services.rooms.RoomsServiceVerticle.clientCommunicationStrategy", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#clientCommunicationStrategy:it.cwmp.services.wrapper.RoomReceiverApiWrapper", "kind" : "implicit val"}, {"label" : "validationStrategy", "tail" : ": Validation[String, User]", "member" : "it.cwmp.services.rooms.RoomsServiceVerticle.validationStrategy", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#validationStrategy:it.cwmp.utils.Validation[String,it.cwmp.model.User]", "kind" : "implicit val"}, {"label" : "log", "tail" : ": Logger", "member" : "it.cwmp.utils.Logging.log", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#log:com.typesafe.scalalogging.Logger", "kind" : "implicit val"}, {"label" : "request", "tail" : "(routingContext: RoutingContext): HttpServerRequest", "member" : "it.cwmp.utils.VertxServer.request", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#request(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):io.vertx.scala.core.http.HttpServerRequest", "kind" : "def"}, {"label" : "getRequestParameter", "tail" : "(paramName: String)(routingContext: RoutingContext): Option[String]", "member" : "it.cwmp.utils.VertxServer.getRequestParameter", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#getRequestParameter(paramName:String)(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):Option[String]", "kind" : "def"}, {"label" : "response", "tail" : "(routingContext: RoutingContext): HttpServerResponse", "member" : "it.cwmp.utils.VertxServer.response", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#response(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):io.vertx.scala.core.http.HttpServerResponse", "kind" : "def"}, {"label" : "sendResponse", "tail" : "(httpCode: Int, message: Option[String])(routingContext: RoutingContext): Unit", "member" : "it.cwmp.utils.VertxServer.sendResponse", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#sendResponse(httpCode:Int,message:Option[String])(implicitroutingContext:io.vertx.scala.ext.web.RoutingContext):Unit", "kind" : "def"}, {"label" : "startFuture", "tail" : "(): Future[_]", "member" : "it.cwmp.utils.VertxServer.startFuture", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#startFuture():scala.concurrent.Future[_]", "kind" : "def"}, {"label" : "server", "tail" : ": HttpServer", "member" : "it.cwmp.utils.VertxServer.server", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#server:io.vertx.scala.core.http.HttpServer", "kind" : "val"}, {"label" : "asJava", "tail" : "(): Verticle", "member" : "io.vertx.lang.scala.ScalaVerticle.asJava", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#asJava():io.vertx.core.Verticle", "kind" : "def"}, {"label" : "processArgs", "tail" : "(): Buffer[String]", "member" : "io.vertx.lang.scala.ScalaVerticle.processArgs", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#processArgs:scala.collection.mutable.Buffer[String]", "kind" : "def"}, {"label" : "config", "tail" : "(): JsonObject", "member" : "io.vertx.lang.scala.ScalaVerticle.config", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#config:io.vertx.core.json.JsonObject", "kind" : "def"}, {"label" : "deploymentID", "tail" : "(): String", "member" : "io.vertx.lang.scala.ScalaVerticle.deploymentID", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#deploymentID:String", "kind" : "def"}, {"label" : "stopFuture", "tail" : "(): Future[_]", "member" : "io.vertx.lang.scala.ScalaVerticle.stopFuture", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#stopFuture():scala.concurrent.Future[_]", "kind" : "def"}, {"label" : "stop", "tail" : "(): Unit", "member" : "io.vertx.lang.scala.ScalaVerticle.stop", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#stop():Unit", "kind" : "def"}, {"label" : "start", "tail" : "(): Unit", "member" : "io.vertx.lang.scala.ScalaVerticle.start", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#start():Unit", "kind" : "def"}, {"label" : "init", "tail" : "(vertx: Vertx, context: Context, verticle: AbstractVerticle): Unit", "member" : "io.vertx.lang.scala.ScalaVerticle.init", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#init(vertx:io.vertx.core.Vertx,context:io.vertx.core.Context,verticle:io.vertx.core.AbstractVerticle):Unit", "kind" : "def"}, {"label" : "ctx", "tail" : ": Context", "member" : "io.vertx.lang.scala.ScalaVerticle.ctx", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#ctx:io.vertx.scala.core.Context", "kind" : "var"}, {"label" : "vertx", "tail" : ": Vertx", "member" : "io.vertx.lang.scala.ScalaVerticle.vertx", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#vertx:io.vertx.scala.core.Vertx", "kind" : "var"}, {"label" : "executionContext", "tail" : ": VertxExecutionContext", "member" : "io.vertx.lang.scala.ScalaVerticle.executionContext", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#executionContext:io.vertx.lang.scala.VertxExecutionContext", "kind" : "implicit var"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#toString():String", "kind" : "def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#clone():Object", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "members_object" : [{"label" : "apply", "tail" : "(validationStrategy: Validation[String, User], clientCommunicationStrategy: RoomReceiverApiWrapper): RoomsServiceVerticle", "member" : "it.cwmp.services.rooms.RoomsServiceVerticle.apply", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#apply(implicitvalidationStrategy:it.cwmp.utils.Validation[String,it.cwmp.model.User],implicitclientCommunicationStrategy:it.cwmp.services.wrapper.RoomReceiverApiWrapper):it.cwmp.services.rooms.RoomsServiceVerticle", "kind" : "def"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#toString():String", "kind" : "def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#clone():Object", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle$.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "class" : "it\/cwmp\/services\/rooms\/RoomsServiceVerticle.html", "kind" : "class"}]};