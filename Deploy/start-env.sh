#!/bin/sh

# Wrapper script for docker-compose that builds/starts the requested environment in one of three
# modes (dev/test/prod).
#
# Each environment has its own dev, test and prod configuration files. See the following files for
# more info:
# ./<env_name>/
#   docker-compose.yml
#   docker-compose.dev.yml
#   docker-compose.test.yml
#   docker-compose.prod.yml
#
# Run this script with no arguments for usage instructions.


# Bail out if the environment name and mode weren't supplied
if [ "$#" -lt 2 ]; then
  echo "============================================================================="
  echo " Usage:"
  echo "  $0 [env_name] [mode] <additional_args_for_docker_compose>"
  echo ""
  echo "  [env_name] : the environment to start (agent/db/web)"
  echo "      [mode] : configuration mode name (dev/test/prod)"
  echo "============================================================================="
  exit 1
fi

# Read env and mode from the first two args
env=$1
mode=$2
shift
shift

# Check that a valid env name was supplied
case $env in
  db) ;;
  agent) ;;
  web) ;;
  *)
    echo "[$env] is not a recognised environment name; choose 'agent', 'db', or 'web'."
    exit 2
esac

# Check that a valid mode was supplied and set default options for 'docker-compose up'
up_default_opts="-d"
case $mode in
  dev)
    up_default_opts="$up_default_opts --force-recreate --build"
    ;;
  test)
    ;;
  prod)
    ;;
  *)
    echo "[$mode] is not a recognised mode - choose 'dev', 'test' or 'prod'."
    exit 3
    ;;
esac

# Set compose files for this mode
compose_files="docker-compose.yml docker-compose.$mode.yml"

# Set args to docker-compose itself, including the file specifiers
compose_file_args=$(echo $compose_files |sed -e 's/ / -f /' -e 's/^/-f /')
compose_opts="$compose_file_args -p $mode-$env"

# Set options for 'docker-compose up', including any additional args passed to this script
up_opts="$up_default_opts $*"

printf "\n==========================================================================================\n"
printf "Building the $env environment in $mode mode\n\n"

# Build in environment dir
pushd $env > /dev/null

# Loop over volumes listed in the compose files, creating them if they don't exist already
echo "Checking/creating required Docker volumes..."
vol_names="$(docker-compose $compose_opts config --volumes|tr '\n' ' ')"
for vol_name in $vol_names; do
  docker volume create $vol_name
done
printf "Done\n\n"

# Loop over secret files listed in the compose files, ensuring that they all exist, and have exactly one word on one linea
echo "Checking required Docker secrets..."
for compose_file in $compose_files; do
  secret_file_paths=$(grep -E "file:.*\/secrets\/.*" $compose_file|awk '{print $2}'|tr "\n" " ")
  for secret_file_path in $secret_file_paths; do
    if [ -f $secret_file_path ]; then
      num_lines=$(wc -l $secret_file_path |awk '{print $1}')
      num_words=$(wc -w $secret_file_path |awk '{print $1}')
      if [ $num_lines != "1" ] || [ $num_words != "1" ]; then
        echo "Secret file at $secret_file_path looks to be invalid (contains $num_words words on $num_lines lines; expected 1 word on 1 line)"
        popd > /dev/null
        exit 2
      fi
    else
      echo Expected secret file not found at $env/$secret_file_path
      popd > /dev/null
      exit 1
    fi
  done
done
printf "Done\n\n"

# Run docker-compose
docker_compose_cmd="docker-compose $compose_opts up $up_opts"
echo "Running $docker_compose_cmd in ./$env ..."
$docker_compose_cmd
compose_up_exit_code=$?
echo Done

# Return from environment dir
popd > /dev/null

if [ $compose_up_exit_code -eq 0 ]; then
  printf "\n$env environment started in $mode mode\n"
else
  printf "\n'docker-compose up' failed with exit code $compose_up_exit_code\n"
fi
printf "==========================================================================================\n"

exit $compose_up_exit_code
