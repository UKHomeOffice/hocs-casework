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

### Summary

A schema view for what additional fields are shown to the user from within the summary tab.

This is split by case type for easier modification and readability.

#### Schema

```json
{
    "type": <<CASE_TYPE>>,
    "fields": [
        {
            "type": <<FIELD_TYPE>>,
            "name": <<CASE_DATA_BLOB_NAME>>,
            "label": <<DISPLAY_NAME>>,
            "choices": <<CHOICES_OBJECT>>,
            "conditionChoices": <<CONDITIONAL_CHOICES_OBJECT>>
        }
    ]
}
```

> The `type` field is optional, `date` and `checkbox` are currently used within the frontend to use a custom renderer.

> `choices` and `conditionChoices` cannot both be used with one another.
>
> An exception will throw if you attempt to use both on the same field object.

#### Choices Schema

There are currently three implementations of choices within the system.

```json
{
    "choices": [
        {
            "label": <<DISPLAY_VALUE>>,
            "name": <<MATCHING_VALUE>>
        }...
    ]
}
```

```json
{
    "choices": <<STATIC_LIST_STRING>>
}
```

The following is currently only used within `somu-list` components.

```json
{
    "choices": {
        <<FIELD_NAME>>: <<STATIC_LIST_STRING>>
    }
}
```

#### Conditional Choice Schema

Conditional choices check for the existence of a value within the case's data blob and uses that choices if satisfied.

```json
{
    "conditionChoices": [
        {
            "choices": <<CHOICES_OBJECT>>
            "conditionPropertyName": <<CASE_DATA_BLOB_NAME>>,
            "conditionPropertyValue": <<CASE_DATA_BLOB_VALUE>>
        }...
    ]
}
```
