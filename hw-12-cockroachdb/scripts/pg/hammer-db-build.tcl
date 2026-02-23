#!/bin/tclsh
dbset db pg
diset connection pg_host localhost
diset connection pg_port 5432
diset connection pg_user postgres
diset connection pg_pass postgres
diset connection pg_sslmode disable
diset tpcc pg_count_ware 200
diset tpcc pg_dbase tpcc
diset tpcc pg_defaultdbase postgres
diset tpcc pg_num_vu 8
diset tpcc pg_superuser postgres
diset tpcc pg_superuserpass postgres
buildschema
waittocomplete
quit