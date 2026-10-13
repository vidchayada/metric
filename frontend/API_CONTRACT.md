# Girlette API contract (Vue frontend ⇄ Spring Boot backend)

Base path `/api`, JSON in **camelCase**. Auth = **JWT** sent as `Authorization: Bearer <token>`.
Roles are `USER` / `ADMIN`. Errors use `{ "message": "...", "errors": { "field": "..." } }`.
Image URLs returned by the API are used as-is, so serve uploaded files from `/uploads/**`.

## Auth & user
| Method | Path | Body → Response | Notes |
|---|---|---|---|
| POST | `/auth/login` | `{username,password}` → `{token,user}` | 401 on bad credentials |
| POST | `/auth/register` | `{username,email,password}` → 201 | 409 + `errors:{username?,email?}` on duplicates. **Hash with BCrypt** |
| GET | `/auth/me` | → `User` | used to restore the session after refresh |
| PUT | `/users/me` | `{fullName,username,email,phone,dateOfBirth,address}` → `User` | 409 → `message` ("Username is already taken.") |
| POST | `/users/me/avatar` | multipart `avatar` (jpeg/png/webp) → `User` | |

`User = {id,username,fullName,email,phone,dateOfBirth,address,profileImage,role}`

## Catalog (public)
| Method | Path | Response |
|---|---|---|
| GET | `/home` | `{bestSellers: ProductSummary[4], categories: CategorySummary[], newestImage}` |
| GET | `/categories` | `CategorySummary[]` (visibility = show only) |
| GET | `/products?page=1&size=15&category=<slug>&q=<text>` | `Page<ProductSummary>`; **404** if the category slug does not exist or is hidden |
| GET | `/products/{id}` | `ProductDetail` or 404 |

`ProductSummary = {id,name,price,image}` · `CategorySummary = {id,name,slug,image}`
`Page<T> = {items,page,totalPages,totalItems}` (page is **1-based**; exclude soft-deleted products)
`ProductDetail = {id,name,description,price,categoryName,images:[{id,url,primary,sortOrder}],attributes:[{id,size,color,stockQty}]}`

## Cart (USER) — scoped to the token's user, never to a client-sent id
| Method | Path | Body → Response |
|---|---|---|
| GET | `/cart` | `CartItem[]` = `{id,productId,attributeId,name,price,quantity,size,color,image}` |
| GET | `/cart/count` | `{count}` (sum of quantities) |
| POST | `/cart/items` | `{productId,attributeId,quantity}` — merge into an existing line, reject quantity > stock |
| PATCH | `/cart/items/{id}` | `{quantity}` |
| DELETE | `/cart/items/{id}` | |
| POST | `/cart/checkout` | → `{orderNumber}`; create order + items, decrement stock, clear cart (one transaction) |

## Newsletter
`POST /subscribers {email}` → `{message}`; 409 `{message:"Email already subscribed"}`.

## Admin (`ROLE_ADMIN`)
| Method | Path | Notes |
|---|---|---|
| GET | `/admin/dashboard` | `{totalMonth,totalWeek,totalCategory,totalProduct,itemSold:[{name,qty}],categories:[{id,name,products:[string]}],recentOrders:[{number,date,customer}],outOfStock:[string]}` |
| GET | `/admin/orders?search&month&status&page&size=8` | `Page<{id,orderNumber,orderDate,orderName,customer,status}>`; status ∈ pending, processing, shipped, completed, cancelled |
| PATCH | `/admin/orders/{id}/status` | `{status}` |
| GET | `/admin/categories?filter=show\|hidden&search=` | `[{id,name,slug,visibility,productCount}]` |
| POST / PUT | `/admin/categories[/{id}]` | `{name,visibility}`; server derives `slug` |
| PATCH | `/admin/categories/{id}/visibility` | `{visibility}` |
| DELETE | `/admin/categories/{id}` | |
| GET | `/admin/products?category=<id>&search=` | `[{id,name,detail,price,categoryId,categoryName,mainImage,totalStock,attributes:[{size,color,stockQty}]}]` |
| POST / PUT | `/admin/products[/{id}]` | multipart: part `data` (JSON `{name,detail,price,categoryId,attributes}`) + parts `images` (optional) |
| DELETE | `/admin/products/{id}` | soft delete (`is_deleted = true`) |
| GET | `/admin/sales?dailyMonth&dailyYear&monthlyYear` | `{topProducts:[{name,imageUrl,totalOrders}],daily:[{label,totalQty,totalSales}],monthly:[...]}` (completed orders only) |

## Setup
- Vite proxies `/api` and `/uploads` to `http://localhost:8080` (see `vite.config.ts`). In production put both behind one origin or enable CORS.
- Do not return `passwordHash` in any response.
