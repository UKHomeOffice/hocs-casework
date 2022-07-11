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

### Priority Policies Fields

A list of policies that should be applied for calculating the general priority of a case. 

This is often utilised by the frontend to calculate the default workstack display.

#### Schema

```json
{
  <<CASE_TYPE>>: [
    {
      "type": <<TYPE_NAME>>,
      "config": {
        <<OPTIONS>>...
      }
    }
  ]
}
```
