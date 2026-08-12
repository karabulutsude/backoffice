/*
Neden DTO (Data Transfer Object) kullanıyoruz?

Çünkü Entity veritabanını temsil eder, DTO ise istemci (Unity) ile haberleşmeyi temsil eder.

Bunu aklında şöyle tutabilirsin:

- Entity → PostgreSQL'de nasıl saklanacağını anlatır.
- DTO → Unity ile hangi bilgilerin gidip geleceğini anlatır.

Gerçek projelerde bu ayrım çok önemlidir.
 */

/*
Backend'in Unity'ye cevap vermesi gerekiyor.

Mesela

Unity gönderiyor

{
    "deviceId":"ABC123"
}

Backend cevap veriyor

{
    "playerId":100001
}

İşte bunun için LoginResponse oluşturuyoruz.
 */


package com.jollifiy.backoffice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {
    private String playerId;
}
