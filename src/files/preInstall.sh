#!/bin/bash

# Safely add the user/group.
getent passwd phone-home >> /dev/null 2>&1 || adduser --system --no-create-home --group phone-home
exit 0
