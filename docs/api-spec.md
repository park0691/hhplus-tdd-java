# API 명세서

## 기본 정보

| 항목 | 내용 |
|------|------|
| Base URL | `http://localhost:8080` |
| Content-Type | `application/json` |
| 오류 응답 공통 형식 | `{ "code": "500", "message": "에러가 발생했습니다." }` |

---

## 엔드포인트

### 1. 포인트 조회

특정 유저의 현재 포인트 잔액을 조회합니다.

```
GET /point/{id}
```

#### Path Variable

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `id` | `long` | Y | 사용자 ID |

#### Response `200 OK`

```json
{
  "id": 1,
  "point": 5000,
  "updateMillis": 1711234567890
}
```

---

### 2. 포인트 충전/이용 내역 조회

특정 유저의 포인트 충전 및 사용 전체 내역을 조회합니다.

```
GET /point/{id}/histories
```

#### Path Variable

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `id` | `long` | Y | 사용자 ID |

#### Response `200 OK`

```json
[
  {
    "id": 1,
    "userId": 1,
    "amount": 5000,
    "type": "CHARGE",
    "updateMillis": 1711234567890
  },
  {
    "id": 2,
    "userId": 1,
    "amount": 2000,
    "type": "USE",
    "updateMillis": 1711234599000
  }
]
```

---

### 3. 포인트 충전

특정 유저의 포인트를 충전합니다.

```
PATCH /point/{id}/charge
```

#### Path Variable

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `id` | `long` | Y | 사용자 ID |

#### Request Body

`Content-Type: application/json`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| (body) | `long` | Y | 충전할 포인트 |

```json
3000
```

#### 비즈니스 규칙

| 규칙 | 내용 |
|------|------|
| 최대 잔액 | 충전 후 잔액이 `1,000,000`을 초과할 수 없습니다. |

#### Response `200 OK`

```json
{
  "id": 1,
  "point": 8000,
  "updateMillis": 1711234599000
}
```

#### Error Response

| 상황 | HTTP Status | message |
|------|-------------|---------|
| 최대 포인트 초과 | `500` | `충전 가능한 포인트 최대값을 초과했습니다.` |

---

### 4. 포인트 사용

특정 유저의 포인트를 사용합니다.

```
PATCH /point/{id}/use
```

#### Path Variable

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `id` | `long` | Y | 사용자 ID |

#### Request Body

`Content-Type: application/json`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| (body) | `long` | Y | 사용할 포인트 |

```json
2000
```

#### 비즈니스 규칙

| 규칙 | 내용 |
|------|------|
| 잔액 부족 | 사용할 포인트가 현재 잔액을 초과할 수 없습니다. |

#### Response `200 OK`

```json
{
  "id": 1,
  "point": 6000,
  "updateMillis": 1711234612000
}
```

#### Error Response

| 상황 | HTTP Status | message |
|------|-------------|---------|
| 포인트 부족 | `500` | `포인트가 부족합니다.` |
