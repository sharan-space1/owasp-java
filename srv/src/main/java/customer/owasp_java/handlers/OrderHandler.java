package customer.owasp_java.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cds.Result;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

import customer.owasp_java.utils.CryptoUtils;

@Component
@ServiceName("OrderService")
public class OrderHandler implements EventHandler {

    @Autowired
    PersistenceService db;

    @On(event = "CREATE", entity = "OrderService.Orders")
    public void onCreate(CdsCreateEventContext context) {

        for (Map<String, Object> data : context.getCqn().entries()) {

            String bookId = (String) data.get("book_ID");
            Integer quantity = (Integer) data.get("quantity");
            Integer amount = (Integer) data.get("amount");
            String currency = (String) data.get("currency");
            String signature = (String) data.get("signature");

            if (signature == null) {
                throw new RuntimeException("Missing signature");
            }

            Result result = db.run(Select.from("BookService.Books").columns("amount", "currency", "stock")
                    .where(b -> b.get("ID").eq(bookId)));

            Map<String, Object> book = result.first().orElseThrow(() -> new RuntimeException("Book not found"));
            Integer stock = (Integer) book.get("stock");

            if (stock < quantity) {
                throw new RuntimeException("Insufficient stock");
            }

            String canonical = bookId + "|" + quantity + "|" + amount + "|" + currency;
            String expectedSig = CryptoUtils.hmacSha256(canonical);

            if (!expectedSig.equals(signature)) {
                throw new RuntimeException("Data tampered !!!");
            }

            db.run(Update.entity("BookService.Books").data("stock", stock - quantity)
                    .where(b -> b.get("ID").eq(bookId)));

            data.put("status", "CONFIRMED");
            data.remove("signature");
        }
    }

    @Before(event = "UPDATE", entity = "OrderService.Orders")
    public void preventDirectStatusUpdate(CdsUpdateEventContext context) {

        boolean statusChanged = context.getCqn().entries().stream()
                .anyMatch(e -> e.containsKey("status"));

        if (statusChanged) {
            throw new RuntimeException("Order status can only be changed via approveOrder action");
        }
    }

    @On(event = "approveOrder")
    public void approveOrder(EventContext context) {

        String orderId = (String) context.get("orderId");
        
        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID is required");
        }
        
        Map<String, Object> order = db.run(Select.from("OrderService.Orders")
                .columns("status")
                .where(o -> o.get("ID").eq(orderId))).first()
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String status = (String) order.get("status");

        if (!"CONFIRMED".equals(status)) {
            throw new RuntimeException("Order cannot be approved in state: " + status);
        }

        db.run(Update.entity("OrderService.Orders")
                .data("status", "APPROVED")
                .where(o -> o.get("ID").eq(orderId)));
        
        context.setCompleted();
    }
}