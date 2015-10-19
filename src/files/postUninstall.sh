#!/bin/bash

# Safely delete the user/group.
getent passwd phone-home >> /dev/null 2>&1 && deluser --system phone-home
exit 0
