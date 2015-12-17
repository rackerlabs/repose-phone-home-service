#!/bin/bash

# Safely add the user/group.
getent passwd repose-phone-home >> /dev/null 2>&1 || adduser --system --no-create-home --group repose-phone-home
exit 0
