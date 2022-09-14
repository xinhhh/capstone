from setuptools import setup, find_packages

setup(
    name='vapourtecagent',
    version='0.0.1',
    author='Jiaru Bai',
    author_email='jb2197@cam.ac.uk',
    license='MIT',
    python_requires='>=3.7',
    description="vapourtecagent is capable of executing the assigned reaction experiment as part of The World Avatar project.",
    url="https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/VapourtecAgent",
    long_description=open('README.md').read(),
    long_description_content_type="text/markdown",
    packages=find_packages(exclude=['tests','tests.*']),
    install_requires=['pyderivationagent>=1.1.0', 'pandas', 'pydantic==1.9.0', 'chemistry_and_robots>=1.0.0'
    # 'agentlogging @ git+https://github.com/cambridge-cares/TheWorldAvatar@main#subdirectory=Agents/utils/python-utils'
    ],
    extras_require={
        "dev": [
            "testcontainers>=3.4.2",
            "pytest>=6.2.3",
            "pytest-docker-compose>=3.2.1",
            "pytest-rerunfailures>=10.2"
        ],
    },
    include_package_data= True
)