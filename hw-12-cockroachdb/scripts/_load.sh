#!/usr/bin/env bash

# PostgreSQL: создание pgbench схемы ~10+ ГБ (scale 800)
pgbench -i -s 800 -h 127.0.0.1 -p 5432 -U postgres postgres # init pgbench dataset

# PostgreSQL: проверка размера БД
psql -h 127.0.0.1 -p 5432 -U postgres -d postgres -c "SELECT pg_size_pretty(pg_database_size('postgres'));" # show db size

# PostgreSQL: нагрузка 10 минут, 64 клиента
pgbench -h 127.0.0.1 -p 5432 -U postgres -c 64 -j 64 -T 600 postgres # run pgbench load

# CockroachDB: инициализация kv workload
cockroach workload init kv --drop --db=kv --splits=100 {pgurl:root@127.0.0.1:26257?sslmode=disable} # init kv schema

# CockroachDB: загрузка >10 ГБ (50 млн строк)
cockroach workload fixtures import kv --rows=50000000 {pgurl:root@127.0.0.1:26257?sslmode=disable} # import large dataset

# CockroachDB: проверка размера ranges
cockroach sql --insecure --host=127.0.0.1:26257 -e "SELECT sum(range_size_mb) FROM [SHOW RANGES FROM DATABASE kv WITH DETAILS];" # show kv size

# CockroachDB: нагрузка 10 минут, 64 клиента, 50% read / 50% write
cockroach workload run kv --concurrency=64 --duration=10m --read-percent=50 {pgurl:root@127.0.0.1:26257?sslmode=disable} # run kv workload

https://chatgpt.com/g/g-p-68cabd1e40948191a4cef1e3cf875e7a-vide-coding/c/699b4c12-7c40-8387-a8af-d6b91a6cc0cb