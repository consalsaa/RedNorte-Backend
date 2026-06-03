# 🏥 Microservicio: ms-listas-espera

Este microservicio se encarga de la gestión de la lista de espera de derivaciones médicas del hospital.

---

## 🏗️ Implementación de Patrón Saga (Coreografía)

Para garantizar la consistencia eventual de los datos en transacciones distribuidas multicentro sin acoplamiento centralizado, este microservicio implementa el **Patrón Saga basado en Coreografía**.

### Estado de la Transacción (`sagaStatus`)
Cada entidad `Atencion` posee un campo `sagaStatus` que define el progreso de la transacción distribuida:
- **`PENDING`**: La atención ha sido creada temporalmente pero está a la espera de que los demás servicios completen sus verificaciones u operaciones asociadas.
- **`CONFIRMED`**: Transacción distribuida finalizada exitosamente. La atención es completamente válida.
- **`CANCELLED`**: La transacción falló en alguna etapa posterior y fue revertida por una transacción compensatoria.

---

## 🔄 Transacciones Compensatorias

En caso de fallos en el motor de reasignaciones o rechazo por parte de otros servicios en la coreografía, se gatilla una **transacción compensatoria** para restaurar el estado del sistema:

1. **Paso 1 (Crear)**: Se invoca `POST /api/listas-espera/saga/crear`. Esto genera un registro de derivación con estado `PENDING`.
2. **Paso 2 (Acciones Distribuidas)**: Se gatillan eventos o llamadas hacia los microservicios `ms-reasignacion` o `ms-portal-paciente`.
3. **Paso 3 (Resultado)**:
   - **Éxito**: Se invoca `PUT /api/listas-espera/saga/confirmar/{id}`, cambiando el estado de la saga a `CONFIRMED`.
   - **Fallo / Compensación**: Si la reasignación u otra validación falla, la coreografía invoca el endpoint de compensación `PUT /api/listas-espera/saga/cancelar/{id}`. Este endpoint marca el `sagaStatus` como `CANCELLED` y cambia el `estado` general de la derivación a `CANCELADO`, realizando un rollback lógico completo del sistema.
