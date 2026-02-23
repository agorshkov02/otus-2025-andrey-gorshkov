#!/bin/tclsh
dbset db pg
diset connection pg_host localhost
diset connection pg_port 5432
diset connection pg_user postgres
diset connection pg_pass postgres
diset connection pg_sslmode disable
diset tpcc pg_allwarehouse true
diset tpcc pg_dbase tpcc
diset tpcc pg_driver timed
diset tpcc pg_duration 3
diset tpcc pg_rampup 1
vuset vu 32
vucreate
vurun
waittocomplete
quit
