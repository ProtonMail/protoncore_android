#Domain Module

Will hold all business logic common for all products, plus will provide an interface for the data
module (currently called api) which should deal with the various data sources. Ideally we will have
more implementation of the domain data interface and have more data modules (data-api, data-local)
that each product can later implement and provide their own implementation (which of course will 
be based on the core-api module).

## Dependencies

No dependencies, pure Java/Kotlin module, testable by only unit and integration tests.
Any other library additions or conversions of this module are prohibited.!
 