#!/bin/bash

echo "Calling the entry-point"

# If not limited by -m (e.g. `docker -m 1G run -it <image_name>`), docker container can use all the host memory.
# In such case CGROUP memory limit returns some huge, clearly irrelevant number.
# So we need to use CGROUP limit if it is set, or host limit otherwise
CGROUP_MEMORY_LIMIT_FILE="/sys/fs/cgroup/memory/memory.limit_in_bytes"
if [ -f $CGROUP_MEMORY_LIMIT_FILE ]; then
    MAXRAM=$(cat ${CGROUP_MEMORY_LIMIT_FILE})
else
    echo "This script is designed to run inside docker only, exiting..."
    exit 1
fi

TOTAL_HOST_MEMORY=$(($(cat /proc/meminfo  |head -n 1 |awk '{print $2}')*1024))

if [ "${MAXRAM}" -lt "${TOTAL_HOST_MEMORY}" ]; then
    TOTAL_MEM=$MAXRAM
else
    TOTAL_MEM=$TOTAL_HOST_MEMORY
fi
TWENTYPERCENT=$((${TOTAL_MEM}/5))
XMX=$(($TOTAL_MEM-$TWENTYPERCENT))
echo "XMX set to $XMX bytes"

echo "Starting the JVM process"
# Start the JVM (passed as CMD)
#export JAVA_OPTS="$JAVA_OPTS -Xmx$XMX"
command="$@ -J-Xmx$XMX"

echo "going to exec $command"
exec $command

