package customer.owasp_java.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.sap.cds.Result;
import com.sap.cds.ql.Select;
import com.sap.cds.services.persistence.PersistenceService;

import customer.owasp_java.utils.CryptoUtils;

@RestController
@RequestMapping("/rest/order")
public class OrderController {

    @Autowired
    PersistenceService db;

    @PostMapping("/prepare")
    public Map<String, Object> prepareOrder(@RequestBody Map<String, Object> req) {

        String bookId = (String) req.get("bookId");
        Integer quantity = (Integer) req.get("quantity");

        Result result = db.run(Select.from("BookService.Books").columns("amount", "currency")
                .where(b -> b.get("ID").eq(bookId)));

        if (result.first().isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        Map<String, Object> book = result.first().get();

        Integer price = (Integer) book.get("amount");
        String currency = (String) book.get("currency");
        Integer totalAmount = price * quantity;
        String canonical = bookId + "|" + quantity + "|" + totalAmount + "|" + currency;

        String signature = CryptoUtils.hmacSha256(canonical);

        return Map.of(
                "bookId", bookId,
                "quantity", quantity,
                "amount", totalAmount,
                "currency", currency,
                "signature", signature);
    }
}