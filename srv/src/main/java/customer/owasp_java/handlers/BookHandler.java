package customer.owasp_java.handlers;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.sap.cds.Result;
import com.sap.cds.ql.Select;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.persistence.PersistenceService;

@Component
@ServiceName("BookService")
public class BookHandler implements EventHandler {

    @Autowired
    private PersistenceService db;

    @On(event = "findByTitle")
    public Result findByTitle(EventContext context) {
        String title = context.get("title") != null ? context.get("title").toString() : null;

        if (title == null || title.trim().isEmpty()) {
            return db.run(Select.from("BookService.Books")
                    .where(b -> b.get("title").eq("")));
        }

        return db.run(Select.from("BookService.Books")
                .where(b -> b.get("title").eq(title)));
    }
}