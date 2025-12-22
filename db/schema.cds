using {
  managed,
  cuid
} from '@sap/cds/common';

namespace com.owasp.java;

entity Books : managed, cuid {
  title  : String;
  stock  : Integer;
  author : Association to Authors;
}

entity Authors : managed, cuid {
  name    : String;
  age     : Integer;
  address : String;
  books   : Association to many Books
              on books.author = $self;
}