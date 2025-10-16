## FaaS система биллинга (Team YT)

## Описание эндпоинтов

# API документация

Базовый URL при локальном запуске: `http://localhost:8080`  
Документацию можно получить по путям /swagger-ui.html,
---

## Содержание

- [Форматы запросов и ответов](#форматы-запросов-и-ответов)
- [Обзор эндпоинтов](#обзор-эндпоинтов)
- [Тарифные планы (`/rateplans`)](#тарифные-планы-rateplans)
- [Бесплатный пакет (`/free-tier`)](#бесплатный-тариф-free-tier)
- [Добавление функции (`/function/add`)](#добавление-функции-functionadd)
- [Инвойс за период (`/invoice/{name}`)](#инвойс-за-период-invoicename)
- [Схемы данных](#схемы-данных)
- [Инструкция для запуска](#инструкция-ждя-запуска)
---

## Форматы запросов и ответов

- Для `POST` используйте `Content-Type: application/json`.

---

## Обзор эндпоинтов

| Метод | Путь                 | Назначение                                   |
|------:|----------------------|----------------------------------------------|
|  GET  | `/rateplans`         | Получить список тарифных планов              |
|  GET  | `/rateplans/{id}`    | Получить тарифный план по его id             |
| POST  | `/rateplans`         | Создать/обновить тарифный план               |
| POST  | `/function/add`      | Зарегистрировать/развернуть функцию          |
|  GET  | `/free-tier`         | Получить список бесплатных пакетов           |
|  GET  | `/free-tier/{id}`    | Получить параметры бесплатного тарифа по id  |
| POST  | `/free-tier`         | Задать параметры бесплатного пакета          |
|  GET  | `/invoice/{name}`    | Получить инвойс за период по идентификатору  |

---

## Тарифные планы (`/rateplans`)

### Получить список тарифов
`GET /rateplans` → `200 OK` с массивом объектов **RatePlans**.

```bash
curl -X GET http://localhost:8080/rateplans
```

**Пример ответа**
```json
[
  {
    "pricePerCall": 0.005,
    "pricePerMsOfExec": 0.000001,
    "pricePerMbMsOfMem": 0.0000002,
    "pricePerMcpuMsOfCpu": 0.0000001,
    "coldStartFee": 0.001
  }
]
```

### Получить тарифов по id
`GET /rateplans/1` → `200 OK` с массивом объектом **RatePlans**.

```bash
curl -X GET http://localhost:8080/rateplans/1
```

**Пример ответа**
```json
  {
    "pricePerCall": 0.005,
    "pricePerMsOfExec": 0.000001,
    "pricePerMbMsOfMem": 0.0000002,
    "pricePerMcpuMsOfCpu": 0.0000001,
    "coldStartFee": 0.001
  }
```

### Создать/обновить тариф
`POST /rateplans` принимает объект **RatePlans**.

```bash
curl -X POST http://localhost:8080/rateplans   -H "Content-Type: application/json"   -d '{"pricePerCall":0.005,"pricePerMsOfExec":0.000001,"pricePerMbMsOfMem":0.0000002,"pricePerMcpuMsOfCpu":0.0000001,"coldStartFee":0.001}'
```

---

## Бесплатный тариф (`/free-tier`)

### Получить параметры free tier
`GET /free-tier` → массив **FreeTierParams**.

```bash
curl -X GET http://localhost:8080/free-tier
```

**Пример ответа**
```json
[
  {
    "freeTierCalls": 100000,
    "freeTierExecutionMs": 600000,
    "freeTierMbMs": 10000000,
    "freeTierMcpuMs": 5000000
  }
]
```

### Получить пакет free tier по id
`GET /free-tier/{id}` → объект **FreeTierParams**.

```bash
curl -X GET http://localhost:8080/free-tier/1
```

**Пример ответа**
```json
  {
    "freeTierCalls": 100000,
    "freeTierExecutionMs": 600000,
    "freeTierMbMs": 10000000,
    "freeTierMcpuMs": 5000000
  }
```

### Создать/обновить параметры free tier
`POST /free-tier` принимает **FreeTierParams**.

```bash
curl -X POST http://localhost:8080/free-tier   -H "Content-Type: application/json"   -d '{"freeTierCalls":100000,"freeTierExecutionMs":600000,"freeTierMbMs":10000000,"freeTierMcpuMs":5000000}'
```

---

## Добавление функции (`/function/add`)

`POST /function/add` принимает объект **FunctionRequest** — описание функции/сервиса для развертывания.

**Пример тела**
```json
{
  "name": "thumbnailer",
  "image": "registry.local/thumbnailer:1.2.3",
  "port": 8080,
  "args": ["--quality=85"],
  "env": { "JAVA_OPTS": "-Xmx512m" },
  "minScale": 1,
  "maxScale": 10,
  "metric": "rps",
  "target": 100,
  "timeoutSeconds": 30
}
```

```bash
curl -X POST http://localhost:8080/function/add   -H "Content-Type: application/json"   -d @function.json
```

> Ответ `200 OK` — тип `object`, структура зависит от реализации (например, статус/ID).

---

## Инвойс за период (`/invoice/{name}`)

`GET /invoice/{name}` — получить агрегированную информацию по биллингу за интервал.

**Параметры**
- `name` *(path, string, обязателен)* — идентификатор (клиент/функция/ревизия).
- `from` *(query, string, обязателен)*
- `to` *(query, string, обязателен)*

**Формат дат/времени**

```
YYYY-MM-DD HH:MM:SS
```

- Между датой и временем — **пробел**.

**Правильно**
```
/invoice/test1?from=2025-10-16%2012:00:00&to=2025-10-16%2012:00:55
```

**Пример запроса**
```bash
curl -G http://localhost:8080/invoice/test1   --data-urlencode "from=2025-10-16 12:00:00"   --data-urlencode "to=2025-10-16 12:00:55"
```
---

## Схемы данных

### RatePlans

| Поле                  | Тип     | Описание                        |
|-----------------------|---------|---------------------------------|
| `pricePerCall`        | number  | Цена за один вызов              |
| `pricePerMsOfExec`    | number  | Цена за 1 мс исполнения         |
| `pricePerMbMsOfMem`   | number  | Цена за 1 MB·ms памяти          |
| `pricePerMcpuMsOfCpu` | number  | Цена за 1 mCPU·ms CPU           |
| `pricePerColdStartMs` | number  | Плата за 1 мс холодного старта  |

### FreeTierParams

| Поле                   | Тип    | Описание                           |
|------------------------|--------|------------------------------------|
| `freeTierCalls`        | number | Бесплатные вызовы                  |
| `freeTierExecutionMs`  | number | Бесплатные миллисекунды CPU-времени|
| `freeTierMbMs`         | number | Бесплатные MB·ms памяти            |
| `freeTierMcpuMs`       | number | Бесплатные mCPU·ms                 |

### FunctionRequest

| Поле             | Тип                  | Описание                              |
|------------------|----------------------|---------------------------------------|
| `name`           | string               | Имя функции                           |
| `image`          | string               | Docker-образ, который будет запущен   |
| `port`           | int32                | Порт, на котором слушает ваш сервис   |
| `args`           | string[]             | Аргументы передаваемые контейнеру     |
| `env`            | map<string,string>   | Переменные окружения для контейнера   |
| `minScale`       | int32                | Максимум реплик (по умолчанию 1)      |
| `maxScale`       | int32                | Максимум реплик (по умолчанию 10)     |
| `metric`         | string               | Авто-масштабирования (rps по дефолту) |
| `target`         | int32                | Целевое значение метрики              |
| `timeoutSeconds` | int32                | Таймаут выполнения (300сек по дефолту)|

---

## Инструкция для запуска

- Выполнить шаги по запуску kubernetes по [ссылке](https://github.com/stanislav-pimenov/knative-hackathon/blob/main/README.md)

- Запустить `run.sh` из корня проекта

- API доступно до http://localhost:8080/swagger-ui/index.html
