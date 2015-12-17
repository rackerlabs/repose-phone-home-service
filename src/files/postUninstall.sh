#!/bin/bash

# Safely delete the user/group.
getent passwd repose-phone-home >> /dev/null 2>&1 && deluser --system repose-phone-home
exit 0
