using {
  managed,
  cuid,
  User
} from '@sap/cds/common';

namespace com.owasp.java;

entity Books : cuid {
  title      : String;
  stock      : Integer;
  amount     : Integer;
  currency   : String;
  author     : Association to Authors;
  createdAt  : Timestamp  @cds.on.insert: $now;
  createdBy  : User       @cds.on.insert: $user;
  modifiedAt : Timestamp  @cds.on.insert: $now   @cds.on.update: $now;
  modifiedBy : User       @cds.on.insert: $user  @cds.on.update: $user;
}

entity Authors : managed, cuid {
  name    : String;
  age     : Integer;
  address : String;
  books   : Association to many Books
              on books.author = $self;
}

entity Orders : managed, cuid {
  amount    : Integer;
  currency  : String;
  status    : String;
  quantity  : Integer;
  book      : Association to one Books;
  signature : String @cds.persistence.skip;
}
