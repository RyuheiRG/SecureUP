# SecureUP

Aplicación Android de seguridad digital personal (proyecto universitario).

## Estructura del repositorio

- **`main`**: cascarón del proyecto. Solo contiene la estructura de carpetas, la configuración de
  Gradle y la documentación inicial. No tiene código funcional.
- **`develop`**: código funcional e integrado. Aquí viven las pantallas, la bóveda, el escáner y
  las pruebas. Es la rama desde la que se publican los releases.
- **`feature/*`**: una rama por funcionalidad, módulo o corrección. Se integran a `develop` mediante
  Pull Request.

## Cómo trabajar

1. `git switch develop` y `git pull --ff-only`.
2. Crear `feature/<tema>` (kebab-case, en inglés).
3. Desarrollar, compilar y probar.
4. `git push -u origin feature/<tema>` y abrir un Pull Request hacia `develop`.

La versión estable se publica como release `vAA.M.m` desde `develop`, nunca desde `main`.

## Estructura de paquetes

```
com.secureup.app/
├── ui/
│   ├── screens/
│   ├── components/
│   ├── navigation/
│   └── theme/
├── viewmodel/
├── data/
│   ├── remote/
│   ├── local/
│   ├── crypto/
│   └── repository/
└── model/
```

Las carpetas vacías se conservan con `.gitkeep`.
