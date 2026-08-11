start docker by docker compose up -d
Login and Enter sqlcmd:
docker exec -it sqlserver-db /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Nguyen@12345" -C
Expected return: 1> 
Then enter sql query without go
then enter go to execute those query
then enter exit to exit sqlcmd
end docker by docker compose down
erase container by docker compose down -v

Create db:
docker exec -it sqlserver-db /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Nguyen@12345" -C -i /init/flower_shop.sql

Clear cache:
docker exec -it spring-redis redis-cli FLUSHALL

Test qwen: 

curl http://localhost:11434/api/chat -d "{
    \"model\": \"qwen\",
    \"messages\": [
        {
        \"role\": \"user\",
        \"content\": \"Who im i?\"
        }
    ],
    \"stream\": false
}"

docker exec -it ollama ollama run qwen
/bye for out of qwen