#!/bin/bash
# D. Nurkowski (danieln@cmclinnovations.com)

AUTHOR="Daniel Nurkowski <danieln@cmclinnovations.com>"
SPATH="$( cd  "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
CREATE_VENV='n'
VENV_NAME='entityrdfizer_venv'
VENV_DIR=$SPATH
DEV_INSTALL=''
PROJ_NAME='entityrdfizer'
DONT_PROMPT='n'

function usage {
    echo "==============================================================================================================="
    echo $PROJECT_NAME" project installation script."
    echo
    echo "Please run the script with following options:"
    echo "---------------------------------------------------------------------------------------------------------------"
    echo " Usage:"
    echo "  -v [-n VENV_NAME -d VENV_DIR -i -e]"
    echo "  -i [-n VENV_NAME -d VENV_DIR -e]"
    echo "  -s"
    echo "  -h"
    echo ""
    echo "Options"
	echo "  -v              : Create the base "$VENV_NAME" virtual environment for the package."
    echo "                    NOTE, if the same named environment already exists it will be REPLACED."
    echo "                    Use the -n VENV_NAME to change the default environment name"
    echo "  -i              : Install the project and its dependencies."
    echo "                    Use the -n VENV_NAME and -d VENV_DIR to install the package to a"
    echo "                    different environment e.g. as a dependency."
	echo "  -n VENV_NAME    : Name of the virtual environment to create and/or install the package to."
    echo "  -d VENV_DIR     : If used with the -v flag - directory to create the virtual at and install"
    echo "                    the package to."
    echo "                    If used with the -i flag - directory of the environment to install the package to."
	echo "  -e              : Enables developer mode installation."
    echo "  -s              : Silent installation, wont prompt for a user input."
	echo "  -h              : Print this usage message."
    echo ""
	echo "Example usage:"
    echo "./install_script.sh -v                            - this will create the base virt. env. for the project"
	echo "./install_script.sh -v -i                         - this will create the base virt. env. for the project"
    echo "                                                    and install it with all its dependencies"
	echo "./install_script.sh -v -i -e                      - this will create the base virt. env. for the project"
    echo "                                                    and install it in developer mode with all its dev-dependencies"
	echo "./install_script.sh -i                            - this will install the project and its dependencies in"
    echo "                                                    the default "$VENV_NAME" environment."
	echo "./install_script.sh -i -n my_env -d my_env_dir    - this will install the project and its dependencies in"
    echo "                                                    the my_env environment. Use it if you want to include the"
    echo "                                                    "$PROJECT_NAME" in a different environment as a dependency"
    echo "./install_script.sh -v -n my_env -i -e            - this will create virtual environment 'my_env' with all project"
	echo "                                                    dependencies and install the project in a developer mode"
	echo "==============================================================================================================="
	read -n 1 -s -r -p "Press any key to continue"
    exit
}

function create_env {
	echo "Creating virtual environment for this project"
    echo "-----------------------------------------------"
    echo
        echo "Creating "$VENV_NAME" virtual environment..."
		if [ -d "$VENV_DIR/$VENV_NAME" ]; then
			rm -r $VENV_DIR"/"$VENV_NAME
		fi
		python -m venv $VENV_DIR"/"$VENV_NAME
		if [ $? -eq 0 ]; then
			echo ""
			echo "    INFO: Virtual environment created."
			echo "-----------------------------------------"
            if [ -d "$VENV_DIR/$VENV_NAME/bin" ]; then
                PYTHON_EXEC=$VENV_DIR/$VENV_NAME/bin/python
            else
                PYTHON_EXEC=$VENV_DIR/$VENV_NAME/Scripts/python
            fi

		else
			echo ""
			echo "    ERROR: Failed to create virtual environment."
			echo "-----------------------------------------"
            read -n 1 -s -r -p "Press any key to continue"
			exit -1
		fi
		echo ""
}


function install_project {
	echo "Installing the project"
    echo "-----------------------------------------------"
    echo
    $PYTHON_EXEC -m pip install --upgrade pip
    $PYTHON_EXEC -m pip install --no-cache-dir --upgrade $DEV_INSTALL $SPATH
    if [[ "${DEV_INSTALL}" == "-e" ]];
    then
        $PYTHON_EXEC -m pip install --no-cache-dir --upgrade -r $SPATH"/"dev_requirements.txt
    fi
    if [ $? -eq 0 ]; then
    	echo ""
    	echo "    INFO: installation complete."
    	echo "-----------------------------------------"
    else
        echo ""
    	echo ""
    	echo "    ERROR: installation failed."
    	echo "-----------------------------------------"
        read -n 1 -s -r -p "Press any key to continue"
        exit -1
    fi

}

# Scan command-line arguments
if [[ $# = 0 ]]
then
   usage
fi
while [[ $# > 0 ]]
do
key="$1"
case $key in
    -h)
     usage;;
    -v) CREATE_VENV='y'; shift;;
	-n) VENV_NAME=$2; shift 2;;
	-d) VENV_DIR=$2; shift 2;;
    -i) INSTALL_PROJ='y'; shift;;
	-e) DEV_INSTALL='-e'; shift;;
    -s) DONT_PROMPT='-y'; shift;;
    *)
	# otherwise print the usage
    usage
    ;;
esac

done

if [[ $CREATE_VENV == 'y' ]]
then
    create_env
fi
if [[ $INSTALL_PROJ == 'y' ]]
then
    install_project
fi

echo
if [[ "${DONT_PROMPT}" != "-y" ]];
then
    read -n 1 -s -r -p "Press any key to continue"
fi
