FROM wpilib/ubuntu-base:18.04

COPY . /inspect_rli/

ENTRYPOINT ["/inspect_rli/run.sh"]
