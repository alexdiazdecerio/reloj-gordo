# Reloj Gordo

Salvapantallas para Android con la hora y el día en grande. Pensado para verlo mientras el móvil carga.

- Modo **Llenar pantalla**: tamaño automático; en vertical, horas encima de minutos; en horizontal, en una línea.
- Tamaño de hora y día ajustables, atenuado opcional y desplazamiento anti-quemado para OLED.
- Sin permisos, sin red, sin dependencias. ~25 KB.

## Instalar

Con [Obtainium](https://github.com/ImranR98/Obtainium), apuntando a este repo (releases de GitHub).

Luego: *Ajustes → Pantalla → Salvapantallas → Reloj Gordo* y en *Cuándo empezar*, «Mientras se carga».

## Compilar

Sin Gradle ni Android Studio: JDK 17+ y `ANDROID_HOME` con `platforms;android-35` y `build-tools;35.0.0`.

```
./build.sh
```

## Publicar versión

Subir `versionCode`/`versionName` en `AndroidManifest.xml` y:

```
git tag v1.2 && git push --tags
```

El workflow necesita dos secrets: `KEYSTORE_B64` (la clave en base64) y `KS_PASS`.
