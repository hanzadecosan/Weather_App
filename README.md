# Hava

Türkiye'nin 81 ili için Kotlin / Jetpack Compose hava durumu uygulaması.
Mevcut yapı: UI → ViewModel → Repository → Retrofit API; durumlar StateFlow ile taşınır.

## Gün ışığı deneyimi

- Şehrin API yanıtındaki saat dilimi kullanılır; telefonun saat diliminden bağımsızdır.
- Gün doğumu ve gün batımından 2 saat önce özel renk paleti ve davet mesajı açılır.
- Olaydan sonraki 20 dakika boyunca şafak / gün batımı görünümü korunur.
- Ekran açıkken zaman 15 saniyede bir kontrol edilir; arka planda zamanlayıcı durur.
- Gece yarısından sonra doğru günün doğuş/batış verileri seçilir.
- Saatlik tahmin mevcut saatin başlangıcından sonraki 12 zaman dilimini gösterir.
- Eksik veriler sıfır yerine “—” olarak görünür. Yaklaşan olay yoksa davet yerine genel mesaj görünür.

## Veri ve durum

Open-Meteo geocoding ve forecast servisleri kullanılır. Şehir eşleştirmesi Türkiye,
normalize edilmiş ad ve il merkezi bilgisiyle yapılır; belirsiz sonuçlar rastgele seçilmez.
Hatay → Antakya, Kocaeli → İzmit, Sakarya → Adapazarı şehir merkezleri kullanılır.
Konum çözümlemesi oturum boyunca bellekte saklanır.

Yeni şehir isteği eski isteği iptal eder; ek istek kimliği kontrolü eski sonuçların
son durumu değiştirmesini engeller. Seçili ekran rememberSaveable, arama SavedStateHandle
ile korunur. Veriler ekran açıkken 10 dakikada bir yenilenir. Yenileme başarısız olursa
son veriler uyarıyla gösterilir; manuel tekrar deneme mümkündür.

## Geliştirme ve kontroller

Projenin Gradle wrapper ve Java araç zinciri ayarları kullanılır. SDK yolu local.properties
üzerinden sağlanır. minSdk 24 için java.time desteği core library desugaring ile etkinleştirilmiştir.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

WeatherBehaviorTest: saat dilimi, gün ışığı sınırları, gece yarısı, eksik veri,
belirsiz konum, istek yarışı ve bağlantı hatası.
WeatherScreenTest: gün batımı daveti, ekran eylemleri, karanlık temada boş durum,
eski şehrin verisinin yeni başlık altında gösterilmemesi.NavigationStateTest: seçili şehrin Activity yeniden oluşturulduğunda korunması.

Servis sözleşmeleri: [Forecast API](https://open-meteo.com/en/docs),
[Geocoding API](https://open-meteo.com/en/docs/geocoding-api).
Android uyumluluğu: [API desugaring](https://developer.android.com/studio/write/java8-support).

## UI ve hareket

Referanslardan esinlenen sıcak/serin renk geçişleri, cam paneller, adaptif şehir
kartları ve uygulamaya gömülü GIF döngüleri kullanılır. UI dosyalarının sorumlulukları,
GIF kaynakları, yeniden üretim komutu ve hareket politikası [UI tasarım notlarında](docs/UI_DESIGN.md)
açıklanır. Veri, repository ve ViewModel akışı korunmuştur.
