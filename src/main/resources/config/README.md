# Configuration Files

To support independent releasable services, this folder contains static configuration as to not have to call other
services for the data.

### Restricted Fields

This holds a collection of field restrictions per case type and permission.

These permissions are applicable to all users that have the permission level and below.

#### Schema

```json
{
 <<CASE_TYPE>>: {
  <<PERMISSION_LEVEL>>: [
   <<FIELD_NAMES>>
  ] 
 }
}
```
