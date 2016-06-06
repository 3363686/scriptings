/**
 * show_ame() simply writes to the Event Log only, since groupDisplayId = "nobaloon".
 * invokeOnEDT_ame is a try to write to log immediately.
 *
*  Created by _ame_ on 12.09.2015 0:52 0:54.
*/
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import liveplugin.CanCallFromAnyThread
import liveplugin.implementation.Misc
import org.jetbrains.annotations.Nullable

//import java.lang.invoke.MethodHandleImpl

import static com.intellij.notification.NotificationType.INFORMATION

class AmePluginUtil{
  private static final String name = "For this class, .name != .toString().substring(6)";
  @CanCallFromAnyThread
  static void invokeOnEDT_ame(Closure closure) {
    def result = null
    ApplicationManager.application.invokeAndWait(new Runnable() {
      @Override void run() {
        //noinspection GrReassignedInClosureLocalVar
        result = closure()
      }
    }, ModalityState.any())
//    (MethodHandleImpl.BindCaller.T) result
  }
  @CanCallFromAnyThread
  static showAme(@Nullable message, @Nullable String title = "",
              NotificationType notificationType = INFORMATION, String groupDisplayId = "nobaloon"){
    invokeOnEDT_ame {
      message = Misc.asString(message)
      // this is because Notification doesn't accept empty messages
      if (message.trim().empty) message = "[empty message]"

      def notification = new Notification(groupDisplayId, title, message, notificationType)
      ApplicationManager.application.messageBus.syncPublisher(Notifications.TOPIC).notify(notification)
    }
  }

}
