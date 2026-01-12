using {com.owasp.java as ns} from '../db/schema';

service BookService {
  @restrict: [
    { grant: ['READ'], to: 'any' },
    { grant: ['CREATE','UPDATE','DELETE'], to: 'admin' }
  ]
  entity Books as projection on ns.Books;
}

service AdminService @(requires: 'admin') {
  entity Authors as projection on ns.Authors;
}

service OrderService {
  @restrict: [
    { grant: ['CREATE','UPDATE','DELETE'], to: 'admin'}
  ]
  entity Orders as projection on ns.Orders;
}