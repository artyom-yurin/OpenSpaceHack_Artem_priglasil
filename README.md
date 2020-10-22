# artem-pozval-app

### Запуск индексера
* Сборка контейнера
```
https://github.com/artyom-yurin/artem-pozval-app.git
cd main
docker build -t main .
```
* Запуск Индексера
```
docker run -it --gpus all -p 8125:8125 main 
```
* Пример запроса 
```
curl -X POST http://localhost:8125/encode \ 
-H 'content-type: application/json' \ 
-d '{"id": 123,"texts": ["hello world"], "is_tokenized": false}'
```

* Пример ответа
```
{
    "id": 123,
    "results": [[768 float-list]],
    "status": 200
}
```