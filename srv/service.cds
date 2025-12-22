using {com.owasp.java as ns} from '../db/schema';

service BookService {
  entity Books as projection on ns.Books;
}

service AdminService @(requires: 'admin') {
  entity Authors as projection on ns.Authors;
}