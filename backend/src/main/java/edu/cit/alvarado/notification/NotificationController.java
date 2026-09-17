package edu.cit.alvarado.notification;
import org.springframework.web.bind.annotation.*; import org.springframework.http.ResponseEntity; import java.util.List;
@RestController @RequestMapping("/api/notifications") @CrossOrigin(origins="http://localhost:5173")
public class NotificationController {private final NotificationRepository repository; public NotificationController(NotificationRepository r){repository=r;}
 @GetMapping public ResponseEntity<List<NotificationView>> getNotifications(){return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc().stream().map(n->new NotificationView(n.getNotificationId(),n.getMessage(),n.getCreatedAt())).toList());}
 public record NotificationView(Long notificationId,String message,java.time.OffsetDateTime createdAt){}
}
