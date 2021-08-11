import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="twautils",
    version="1.0.0",
    author="CMCL Innovations",
    author_email="support@cmclinnovations.com",
    description="TheWorldAvatar Utilities",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/cambridge-cares/TheWorldAvatar",
    packages=['twautils', 'twautils.log'],
    classifiers=(
        "Programming Language :: Python :: 3",
        "Operating System :: OS Independent",
    ),
)