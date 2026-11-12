# Informe de corrección

Este informe argumenta que las funciones de los Puntos 1 y 2 hacen lo que pide
el enunciado. Como un `Registro` es una lista, es un conjunto definido
recursivamente y se usa inducción estructural: el caso base es el registro
vacío y el paso de inducción añade un sismo al frente.

## Convenciones

* Un `Registro` es o bien la lista vacía $`[\,]`$, o bien $s :: r'$: un sismo $s$
  al frente de un registro $r'$.
* De un sismo $s$ se usan $s\text{.region}$ y $s\text{.magnitud}$.
* $|L|$ es la longitud de la lista $L$, $L_1 \mathbin{++} L_2$ es la
  concatenación y $x \in L$ significa que $x$ aparece en $L$.
* Decimos que $r'$ es un **reordenamiento** de $r$ si tiene los mismos sismos,
  cada uno el mismo número de veces, aunque en otro orden.

### Lo que se supone de la biblioteca

Las operaciones de listas que usa el código cumplen estas ecuaciones:

```math
\begin{aligned}\text{filter}(p, [\,]) &= [\,] \\
\text{filter}(p, x :: xs) &= \begin{cases} x :: \text{filter}(p, xs) & \text{si } p(x) \\ \text{filter}(p, xs) & \text{si no} \end{cases} \\
\text{map}(f, [\,]) &= [\,] \\
\text{map}(f, x :: xs) &= f(x) :: \text{map}(f, xs) \\
\text{flatMap}(f, [\,]) &= [\,] \\
\text{flatMap}(f, x :: xs) &= f(x) \mathbin{++} \text{flatMap}(f, xs)
\end{aligned}
```

* `e.withFilter(p).map(f)` produce el mismo resultado que
  $\text{map}(f, \text{filter}(p, e))$, y dos `withFilter` seguidos equivalen a un
  `filter` con la conjunción de las dos condiciones.
* `distinct` conserva la primera aparición de cada elemento y el orden en que
  aparecen.
* `r.groupBy(g)` es el mapa cuyo dominio es $`\{ g(s) \mid s \in r \}`$ y que a
  cada llave $k$ le asigna

```math
\text{groupBy}(g, r)(k) = \text{filter}(s \mapsto g(s) = k,\ r)
```

Las comparaciones entre magnitudes se hacen en aritmética de `Double`: los
teoremas hablan del valor que el programa calcula, y no de los reales exactos.

## Punto 1: consultas con expresiones `for`

### Lema 1: filtrar y proyectar

Sea $p$ un predicado, $g$ una función y $L(r) = \text{map}(g, \text{filter}(p, r))$.
Entonces:

```math
L([\,]) = [\,] \qquad L(s :: r') = \begin{cases} g(s) :: L(r') & \text{si } p(s) \\ L(r') & \text{si no} \end{cases}
```

*Demostración.* Para el registro vacío,
$`L([\,]) = \text{map}(g, \text{filter}(p, [\,])) = \text{map}(g, [\,]) = [\,]`$.
Para $s :: r'$ hay dos casos. Si $p(s)$, entonces
$\text{filter}(p, s :: r') = s :: \text{filter}(p, r')$, y
$L(s :: r') = g(s) :: \text{map}(g, \text{filter}(p, r')) = g(s) :: L(r')$. Si no
$p(s)$, entonces $\text{filter}(p, s :: r') = \text{filter}(p, r')$ y
$L(s :: r') = L(r')$. $\blacksquare$

### Teorema 1: `fuertes`

Sea $F_1$ la función que el enunciado describe: las regiones de los sismos que
superan el umbral, en orden y con repetidos.

```math
F_1([\,], u) = [\,] \qquad F_1(s :: r', u) = \begin{cases} s\text{.region} :: F_1(r', u) & \text{si } s\text{.magnitud} > u \\ F_1(r', u) & \text{si no} \end{cases}
```

Vamos a demostrar que $\forall u,\ \forall r \in \text{Registro} : \texttt{fuertes}(r, u) == F_1(r, u)$.

La función se traduce a `r.withFilter(s => s.magnitud > umbral).map(s => s.region)`,
es decir, $\text{map}(g, \text{filter}(p, r))$ con $p(s) \equiv s\text{.magnitud} > u$ y
$g(s) = s\text{.region}$. Por inducción sobre $r$, con $u$ fijo:

**Caso base:** $`r = [\,]`$. Por el Lema 1, $`\texttt{fuertes}([\,], u) = [\,] = F_1([\,], u)`$.

**Caso de inducción:** $r = s :: r'$. La hipótesis de inducción (HI) es
$\texttt{fuertes}(r', u) == F_1(r', u)$. Si $s\text{.magnitud} > u$, por el Lema 1:

```math
\texttt{fuertes}(s :: r', u) = s\text{.region} :: \texttt{fuertes}(r', u) \overset{\text{HI}}{=} s\text{.region} :: F_1(r', u) = F_1(s :: r', u)
```

Si no, por el Lema 1 y la HI,
$\texttt{fuertes}(s :: r', u) = \texttt{fuertes}(r', u) = F_1(r', u) = F_1(s :: r', u)$.

Concluimos por inducción que $\texttt{fuertes}(r, u) == F_1(r, u)$ para todo registro y todo
umbral. $\blacksquare$

De la definición de $F_1$ se leen las propiedades que pide el enunciado: el
umbral es **estricto** (se usa $>$), el orden es el del registro y los
repetidos se conservan, porque cada sismo fuerte aporta su región sin
comparar con los demás.

### Teorema 2: `enRegion`

Sea $F_2$ la función que describe el enunciado: las magnitudes de los sismos de la región $x$.

```math
F_2([\,], x) = [\,] \qquad F_2(s :: r', x) = \begin{cases} s\text{.magnitud} :: F_2(r', x) & \text{si } s\text{.region} = x \\ F_2(r', x) & \text{si no} \end{cases}
```

La función se traduce a `r.withFilter(s => s.region == region).map(s => s.magnitud)`,
que es $\text{map}(g, \text{filter}(p, r))$ con $p(s) \equiv s\text{.region} = x$ y
$g(s) = s\text{.magnitud}$. La demostración es la del Teorema 1 con esos $p$ y
$g$: el caso base es el registro vacío, que da $`[\,]`$ por el Lema 1, y el paso
añade un sismo $s$ al frente, donde el Lema 1 y la HI dan
$\texttt{enRegion}(s :: r', x) = F_2(s :: r', x)$ en cada uno de los dos casos.
Por lo tanto $\forall r,\ \forall x : \texttt{enRegion}(r, x) == F_2(r, x)$. En particular,
si la región no aparece, ningún sismo cumple $p$ y el resultado es $`[\,]`$. $\blacksquare$

### Lema 2: cuándo un `filter`, un `map` o un `flatMap` da una lista no vacía

Para toda lista $l$:

```math
\text{filter}(p, l) \neq [\,] \iff \exists x \in l : p(x) \qquad
\text{flatMap}(f, l) \neq [\,] \iff \exists x \in l : f(x) \neq [\,]
```

y $\text{map}(f, l)$ es vacía si y solo si $l$ es vacía, porque $\text{map}$
conserva la longitud.

*Demostración.* Por inducción sobre $l$. Para $`l = [\,]`$, las tres listas son
vacías y no existe ningún $x \in l$. Para $l = x :: xs$, el $\text{flatMap}$ vale
$f(x) \mathbin{++} \text{flatMap}(f, xs)$, que es no vacío si y solo si $f(x)$ es
no vacío o $\text{flatMap}(f, xs)$ es no vacío. Por la HI, esto ocurre si y solo
si $`f(x) \neq [\,]`$ o existe $y \in xs$ con $`f(y) \neq [\,]`$, es decir, si y solo
si existe un elemento de $x :: xs$ con esa propiedad. El caso del
$\text{filter}$ es igual, mirando sus dos ecuaciones. $\blacksquare$

### Teorema 3: `paresCercanos`

Sea $\text{regs}(r) = [a_0, a_1, \ldots, a_{k-1}]$ la lista de las regiones de $r$
sin repetir, en el orden de su primera aparición. Sea

```math
C(a, b, r, d) \equiv \exists s_1, s_2 \in r : s_1\text{.region} = a \land s_2\text{.region} = b \land |s_1\text{.magnitud} - s_2\text{.magnitud}| \leq d
```

la condición "algún sismo de $a$ y algún sismo de $b$ difieren a lo sumo $d$". La
función que describe el enunciado es

```math
P(r, d) = \big[\ (a_i, a_j)\ \big|\ 0 \leq i < j \leq k-1 \ \land\ C(a_i, a_j, r, d)\ \big]
```

con las parejas ordenadas por $i$ y, a igual $i$, por $j$.

Vamos a demostrar que $\forall r,\ \forall d : \texttt{paresCercanos}(r, d) == P(r, d)$.
Lo hacemos en tres pasos.

**Paso 1: `regiones` es $\text{regs}(r)$.** `regiones` se traduce a
`r.map(s => s.region).distinct`. El `map` da la región de cada sismo en el orden
del registro y `distinct` conserva la primera aparición de cada una, así que
`regiones` $= [a_0, \ldots, a_{k-1}]$. Como no tiene repetidos, la única
aparición de $a_i$ está en la posición $i$, y por lo tanto
`regiones.indexOf(` $a_i$ `)` $= i$.

**Paso 2: `cercanas(a, b)` es verdadera si y solo si $C(a, b, r, d)$.** Su
traducción es

```math
\text{flatMap}\big(s_1 \mapsto \text{map}(s_2 \mapsto (s_1, s_2),\ \text{filter}(q_{s_1}, r)),\ r\big)
```

con $q_{s_1}(s_2) \equiv s_1\text{.region} = a \land s_2\text{.region} = b \land |s_1\text{.magnitud} - s_2\text{.magnitud}| \leq d$.
Por el Lema 2, `.nonEmpty` es verdadero si y solo si existe $s_1 \in r$ tal
que $\text{map}(\ldots, \text{filter}(q_{s_1}, r))$ no es vacía, si y solo si existe
$s_1 \in r$ tal que $\text{filter}(q_{s_1}, r)$ no es vacía, si y solo si existen
$s_1, s_2 \in r$ con $q_{s_1}(s_2)$. Eso es exactamente $C(a, b, r, d)$.

**Paso 3: el `for` externo.** Se traduce a

```math
\text{flatMap}\big(a \mapsto \text{map}(b \mapsto (a, b),\ \text{filter}(q'_a, \text{regiones})),\ \text{regiones}\big)
```

con $q'_a(b) \equiv \text{indexOf}(a) < \text{indexOf}(b) \land \texttt{cercanas}(a, b)$.
Por las ecuaciones de $\text{flatMap}$, el resultado es la concatenación, para
$a = a_0, a_1, \ldots$ en ese orden, de los bloques
$B_i = \text{map}(b \mapsto (a_i, b), \text{filter}(q'_{a_i}, \text{regiones}))$. Por los
Pasos 1 y 2, $q'_{a_i}(a_j) \iff i < j \land C(a_i, a_j, r, d)$. Entonces $B_i$ contiene,
en el orden de $j$, las parejas $(a_i, a_j)$ con $j > i$ y $C(a_i, a_j, r, d)$.
Concatenar los bloques en orden creciente de $i$ da las parejas ordenadas por
$i$ y luego por $j$, que es $P(r, d)$. $\blacksquare$

Las propiedades que pide el enunciado salen de esta descripción:

* **Nunca una región consigo misma:** solo hay parejas con $i < j$, así que
  $a_i \neq a_j$.
* **Cada pareja sale una sola vez, sin importar el orden:** las parejas
  corresponden una a una a pares de posiciones $(i, j)$ con $i < j$, porque las
  $a_i$ son distintas, y cada par de posiciones aparece una sola vez. La pareja
  $(a_j, a_i)$ exigiría $j < i$, así que nunca aparece junto con $(a_i, a_j)$.
* **Orden de aparición:** cada pareja se ordena por la posición de la primera
  aparición de sus regiones en el registro.
* **Registro vacío o una sola región:** $k = 0$ o $k = 1$ no tiene pares
  $i < j$, y el resultado es $`[\,]`$.

## Punto 2: agrupar con mapas

Para una región $x$ y un registro $r$ se definen

```math
S(x, r) = \text{filter}(s \mapsto s\text{.region} = x,\ r) \qquad R(r) = \{ s\text{.region} \mid s \in r \}
```

es decir, los sismos de $x$ en el orden del registro, y las regiones que
aparecen. Lo que describe el enunciado es:

* `porRegion(r)` es el mapa de dominio $R(r)$ con $\texttt{porRegion}(r)(x) = S(x, r)$.
* `conteoPorRegion(r)` es el mapa de dominio $R(r)$ con valor $|S(x, r)|$.
* `magnitudMaxima(r)` es el mapa de dominio $R(r)$ con valor
  $`\max \{ s\text{.magnitud} \mid s \in S(x, r) \}`$.

En los tres, las regiones que no están en el registro no son llaves del mapa.

### Teorema 4: `porRegion`

El código es `r.groupBy(_.region)`. Por la especificación de `groupBy` con
$g(s) = s\text{.region}$, el dominio es $`\{ s\text{.region} \mid s \in r \} = R(r)`$ y el
valor de la llave $x$ es $\text{filter}(s \mapsto s\text{.region} = x, r) = S(x, r)$.
Eso es lo que se pedía. Si $x \notin R(r)$, no es una llave, y para el registro
vacío el dominio es vacío, es decir, el resultado es `Map()`. $\blacksquare$

### Lema 3: las llaves tienen al menos un sismo

Si $x \in R(r)$, entonces $`S(x, r) \neq [\,]`$.

*Demostración.* Como $x \in R(r)$, existe $s \in r$ con $s\text{.region} = x$. Por el
Lema 2, $\text{filter}(s \mapsto s\text{.region} = x, r)$ no es vacío. $\blacksquare$

### Teorema 5: `conteoPorRegion`

El código es `porRegion(r).map { case (region, sismos) => (region, sismos.length) }`.
Por el Teorema 4, las entradas de `porRegion(r)` son $(x, S(x, r))$ con
$x \in R(r)$. El `map` reemplaza cada una por $(x, |S(x, r)|)$. Como las llaves
$x$ son distintas, ninguna entrada se pierde ni se mezcla con otra. El resultado
tiene dominio $R(r)$ y valor $|S(x, r)|$. $\blacksquare$

### Teorema 6: `magnitudMaxima`

El código es
`porRegion(r).map { case (region, sismos) => (region, sismos.map(_.magnitud).max) }`.
Cada entrada $(x, S(x, r))$ pasa a $(x, \max(\text{map}(\text{magnitud}, S(x, r))))$. El `max`
está bien definido porque, por el Lema 3, la lista $S(x, r)$ no es vacía para toda
llave, así que no se lanza ninguna excepción. El valor es la mayor magnitud entre
los sismos de $x$, con dominio $R(r)$. $\blacksquare$

### Resultado independiente del orden de recorrido

#### Lema 4: el conteo es una suma de términos independientes

$`|S(x, r)| = \sum_{s \in r} [\,s\text{.region} = x\,]`$, donde el corchete vale 1 si la
condición se cumple y 0 si no.

*Demostración.* Por inducción sobre $r$. Para $`r = [\,]`$, $`S(x, [\,]) = [\,]`$ y su
longitud es 0, como la suma vacía. Para $r = s :: r'$, por la ecuación del
$\text{filter}$, $S(x, s :: r')$ es $s :: S(x, r')$ si $s\text{.region} = x$ y
$S(x, r')$ si no. Entonces
$`|S(x, s :: r')| = [\,s\text{.region} = x\,] + |S(x, r')|`$, que por la HI es el
sumando de $s$ más la suma sobre $r'$. $\blacksquare$

#### Teorema 7: reordenar el registro no cambia los agrupamientos

Sea $r'$ un reordenamiento de $r$. Entonces:

1. $R(r') = R(r)$.
2. $\texttt{conteoPorRegion}(r') = \texttt{conteoPorRegion}(r)$.
3. $\texttt{magnitudMaxima}(r') = \texttt{magnitudMaxima}(r)$.
4. Para toda región $x$, la lista $\texttt{porRegion}(r')(x)$ es un reordenamiento de
   $\texttt{porRegion}(r)(x)$.

*Demostración.*

* **(1)** $R$ solo depende de qué sismos hay, y no del orden.
* **(4)** $S(x, r')$ contiene exactamente los sismos de $r'$ cuya región es $x$.
  Como $r'$ tiene los mismos sismos que $r$ con las mismas repeticiones,
  $S(x, r')$ tiene los mismos sismos que $S(x, r)$ con las mismas repeticiones.
  El orden dentro de la lista sí puede cambiar, y por eso (4) dice
  "reordenamiento" y no "igual".
* **(2)** Por el Lema 4, $|S(x, r)|$ es una suma con un sumando por sismo, y cada
  sumando depende solo de ese sismo, no de su posición. La suma de números enteros
  es conmutativa y asociativa, así que vale lo mismo para $r$ y para $r'$. Con el
  mismo dominio por (1), los dos mapas son iguales.
* **(3)** La mayor magnitud $m$ de una lista no vacía es el único valor que cumple
  (i) aparece en la lista y (ii) es mayor o igual que todos los elementos. Ambas
  condiciones solo dependen de qué valores hay, no de su orden. Por (4), las listas
  $S(x, r)$ y $S(x, r')$ tienen los mismos sismos, y por tanto las mismas
  magnitudes, así que tienen el mismo máximo. Con el mismo dominio por (1), los
  dos mapas son iguales. $\blacksquare$

Por lo tanto, el conteo y la magnitud máxima no dependen del orden en que se
recorre el registro. `porRegion` conserva los mismos sismos por región, pero el
orden dentro de cada lista sigue al del registro: si el registro se invierte,
cada lista queda invertida.

#### Comprobación con pruebas

Dos pruebas de `RegistroSismicoTestExtras` verifican este resultado sobre un
registro concreto: `el conteo y la máxima no dependen del orden en que se recorre
el registro` y `porRegion: con el registro invertido cada región tiene los mismos
sismos`.


## Punto 3 — consultas seguras

### 3.1 `conteoSeguro`

**Especificación.** Versión total de la función de conteo $C$:

$$
CS(r)(reg) =
\begin{cases}
C(r)(reg) & \text{si } reg \in \text{dom}(C(r)) \\
0 & \text{en otro caso}
\end{cases}
$$

**Teorema.** $\text{conteoSeguro}(r) = CS(r)$, y en particular la
aplicación a cualquier región jamás lanza excepción.

**Demostración.** `conteoSeguro(r)` se implementa como `conteoPorRegion(r).withDefaultValue(0)`.
Por el Teorema 5, el mapa subyacente es $C(r)$. Por especificación del método
`withDefaultValue`, ante una clave $k \in \text{dom}(C(r))$ se retorna $C(r)(k)$, y ante
cualquier clave $k \notin \text{dom}(C(r))$ se devuelve el valor por defecto $0$,
sin lanzar `NoSuchElementException`. Esto coincide exactamente con la definición por
casos de $CS(r)$. Si $r = [\,]$, el dominio es vacío y cualquier consulta devuelve $0$. $\blacksquare$

### 3.2 `maximaDe`

**Especificación.**

$$
MD(r, reg) =
\begin{cases}
\text{Some}(M(r)(reg)) & \text{si } reg \in R(r) \\
\text{None} & \text{en otro caso}
\end{cases}
$$

donde $M(r) = \texttt{magnitudMaxima}(r)$.

**Teorema.** $\text{maximaDe}(r, reg) = MD(r, reg)$.

**Demostración.** La función se define como `magnitudMaxima(r).get(region)`.
Por el Teorema 6, `magnitudMaxima(r)` tiene dominio $R(r)$ y asocia a cada región
su máxima magnitud observada. El método `.get(k)` de `Map` retorna `Some(v)` si
$k \in \text{dom}$ y `None` si $k \notin \text{dom}$. Por lo tanto, devuelve
`Some(M(r)(reg))` cuando $reg \in R(r)$ y `None` cuando no se registran sismos de
dicha región o cuando el registro es vacío. No existe caso que invoque evaluación
insegura ni lance excepción. $\blacksquare$

### 3.3 `regionMasActiva`

**Especificación.**

$$
RA(r) =
\begin{cases}
\text{Some}\bigl(\max\nolimits_{\preceq} \text{dom}(C(r))\bigr) & \text{si } r \neq [\,] \\
\text{None} & \text{si } r = [\,]
\end{cases}
$$

donde $\preceq$ es el orden total lexicográfico que compara primero por conteo
descendente y desempata por orden alfabético ascendente:

$$
a \preceq b \iff C(r)(a) > C(r)(b) \;\lor\; \bigl(C(r)(a) = C(r)(b) \land a \le_{\text{alf}} b\bigr)
$$

**Teorema.** $\text{regionMasActiva}(r) = RA(r)$.

**Demostración.** La función convierte el mapa de conteo en lista de tuplas con
`toList`, la ordena mediante `sortBy { case (region, n) => (-n, region) }` y
extrae la cabeza con `headOption.map(_._1)`. La tupla de ordenamiento $(-n, region)$
satisface:
1. Al comparar enteros, $-n_1 < -n_2 \iff n_1 > n_2$, lo que ordena en forma
   descendente según el número de sismos.
2. Ante igualdad en el conteo ($n_1 = n_2$), se desempata por la relación de orden
   alfabético estándar sobre las cadenas de texto (`region`).

Como la relación $\preceq$ inducida por esta tupla es un orden total estricto y el
ordenamiento es estable, el primer elemento es único y corresponde a
$\max_{\preceq} \text{dom}(C(r))$. Si $r = [\,]$, la lista de tuplas es vacía y
`headOption` retorna `None`. $\blacksquare$

**Independencia del orden de recorrido.**
El resultado de $\text{regionMasActiva}(r)$ no depende del orden en que aparezcan
los sismos en $r$:

$$
\forall \pi.\; RA(\pi(r)) = RA(r)
$$

Esto se debe a que $C(\pi(r)) = C(r)$ (Teorema 7) y el criterio de selección y
desempate $\preceq$ opera exclusivamente sobre el par $(\text{conteo}, \text{nombre de región})$,
sin utilizar la posición ni el índice original de los sismos en el registro. $\blacksquare$

## Punto 4 — réplicas perezosas

### 4.1 `replicas`

**Especificación.** La secuencia infinita $(m_k)_{k \ge 1}$ definida por:

$$
m_1 = \rho(\mu), \qquad m_{k+1} = \rho(0.8 \cdot m_k),
\qquad \rho(x) = \frac{\text{round}(100\,x)}{100}
$$

donde $\mu$ es la magnitud inicial dada y $\text{round}$ es el redondeo al entero
más cercano.

**Teorema.** Para todo $n \ge 1$ y toda magnitud $\mu$:

$$
\text{replicas}(\mu).\text{take}(n).\text{toList} = [m_1, m_2, \ldots, m_n]
$$

**Demostración (por inducción sobre $n$).**

* **Caso base ($n = 1$):** `replicas(μ)` evalúa la cabeza como $m_1 = \rho(\mu)$ y
  construye `m #:: replicas(m * 0.8)`. Al aplicar `take(1)`, se retorna
  únicamente $[m_1]$ sin evaluar la cola. $\checkmark$
* **Paso inductivo ($n \to n+1$):**
  La definición perezosa con `#::` tiene cabeza $m_1$ y cola por nombre
  `replicas(m_1 * 0.8)`. Por definición, la magnitud inicial de la cola es
  $\mu' = 0.8 \cdot m_1$, cuya primera cabeza evaluada será
  $m_2 = \rho(\mu') = \rho(0.8 \cdot m_1)$. Aplicando la hipótesis de inducción
  sobre la cola para $n$ elementos:

$$
\text{cola}.\text{take}(n) = [m_2, m_3, \ldots, m_{n+1}]
$$

Por la propiedad de `take` en secuencias con cons perezoso:

$$
\text{replicas}(\mu).\text{take}(n+1) = m_1 :: (\text{cola}.\text{take}(n)) = [m_1, m_2, \ldots, m_{n+1}] \quad \checkmark
$$

$\blacksquare$

**Por qué no agota la memoria.**
El constructor `#::` recibe el segundo argumento por nombre (`=> LazyList[A]`).
Por lo tanto, la llamada recursiva `replicas(m * 0.8)` queda encapsulada como una
suspensión computacional. Al solicitar `take(n)`, el runtime de Scala solo
materializa $n$ celdas en memoria, memorizando cada nodo ya calculado. La memoria
empleada es estrictamente proporcional a los $n$ elementos demandados ($O(n)$) y
nunca al tamaño infinito de la secuencia conceptual.

### 4.2 `replicasSensibles`

**Especificación.** $RS(\mu, c)$ es la lista de réplicas que superan
estrictamente el umbral mínimo $c$, incluyendo la primera magnitud cuando esta lo supera:

$$
RS(\mu, c) = [m_1, \ldots, m_k], \quad k = \max \{ j \ge 0 \mid \forall i \in \{1, \ldots, j\}, m_i > c \}
$$

**Teorema.** $\text{replicasSensibles}(\mu, c) = RS(\mu, c)$ para todo $c \ge 0.02$ y $\mu > 0$.

**Demostración.** La función es `replicas(μ).takeWhile(_ > c).toList`.
Por el Teorema del §4.1, `replicas(μ)` genera sucesivamente la secuencia
$m_1, m_2, \ldots$. La función de orden superior `takeWhile(p)` sobre una
`LazyList` inspecciona cada elemento secuencialmente y detiene la evaluación
inmediatamente en el primer índice $k+1$ tal que $p(m_{k+1})$ sea falso ($m_{k+1} \le c$).
Al detenerse, no fuerza la evaluación de ningún elemento subsiguiente. Luego,
`toList` convierte el prefijo de longitud $k$ en una `List` finita inmutable.
Si $m_1 \le c$, la primera condición falla y retorna $[\,]$. $\blacksquare$

**Garantía de convergencia y terminación.**
Para asegurar que `takeWhile` termine, la sucesión debe alcanzar un elemento menor
o igual a $c$. Para magnitudes $m_k > 0.025$:

$$
m_{k+1} \le 0.8 \cdot m_k + 0.005 < m_k \iff 0.2 \cdot m_k > 0.005 \iff m_k > 0.025
$$

La sucesión decrece monótonamente a tasa geométrica hasta alcanzar el rango
$(0, 0.025]$, donde el redondeo fija el valor en a lo sumo $0.02$
($\rho(0.8 \times 0.02) = \rho(0.016) = 0.02$). Para cualquier umbral sensible
convencional ($c \ge 0.02$), la sucesión cruzará el umbral en un número finito de
pasos $O(\log_{0.8}(c / \mu))$, asegurando que la evaluación perezosa se detiene
siempre. $\blacksquare$

## Punto 5: informe combinado

### Especificación

Se usa la notación de los puntos anteriores: $R(r)$ son las regiones que
aparecen en $r$, $S(x, r)$ los sismos de la región $x$ en el orden del registro,
$M(r) = \texttt{magnitudMaxima}(r)$, $C(r) = \texttt{conteoPorRegion}(r)$ y
$RS(\mu, c)$ la lista de réplicas de una magnitud $\mu$ que superan
estrictamente $c$ (Punto 4). Lo que describe el enunciado es el mapa $I(r, c)$
de dominio $R(r)$ tal que, para cada $x \in R(r)$:

```math
I(r, c)(x) = \texttt{Resumen}\big(\,|S(x, r)|,\ M(r)(x),\ |RS(M(r)(x),\, c)|\,\big)
```

es decir: el número de sismos de la región, su magnitud máxima y el número de
réplicas esperadas del sismo más fuerte.

**Precondición.** $c \geq 0$. Para un mínimo negativo ninguna réplica lo
alcanzaría nunca y `takeWhile` no terminaría (ver el Lema 5).

### Lema 5: `replicasSensibles` termina

El código final de `replicas` redondea solo al emitir: la sucesión interna es
$a_k = \mu \cdot 0.8^k$, sin redondear, y la que se emite es
$m_k = \rho(a_k)$ con $\rho(x) = \text{round}(100\,x)/100$.

**Lema 5.** Para toda magnitud $\mu \geq 0$ y todo $c \geq 0$, existe $K$ tal que
$m_K \leq c$. Por lo tanto `takeWhile(_ > c)` se detiene y
`replicasSensibles(μ, c)` termina.

*Demostración.* Como $0 < 0.8 < 1$, $a_k = \mu \cdot 0.8^k \to 0$. Entonces existe
$K$ con $a_K < 0.005$, y para ese $K$ se tiene $\rho(a_K) = 0 \leq c$. Además,
$\rho$ es monótona y $a_k$ es decreciente, así que $m_k$ es no creciente:
$m_{k+1} \leq m_k$. Entonces la lista emitida es un prefijo de elementos
$> c$ seguido de elementos $\leq c$, y `takeWhile` toma justo ese prefijo,
que es finito, de longitud menor o igual que $K$. $\blacksquare$

La demostración no usa un redondeo en cada paso: es lo que garantiza que el
caso $c = 0$ termine.

### Teorema 8: `informe`

Vamos a demostrar que $\forall r,\ \forall c \geq 0 : \texttt{informe}(r, c) == I(r, c)$.

*Demostración.* El código es

```scala
val maximas = magnitudMaxima(r)
conteoPorRegion(r).map { case (region, n) =>
  (region, Resumen(n, maximas(region), replicasSensibles(maximas(region), minimo).length))
}
```

Lo hacemos en cuatro pasos.

1. **Dominio.** Por el Teorema 5, `conteoPorRegion(r)` es el mapa de dominio
   $R(r)$ con valor $|S(x, r)|$. El `map` recorre exactamente sus entradas
   $(x, |S(x, r)|)$, una por cada $x \in R(r)$. Como las llaves son distintas,
   el `map` no pierde ni mezcla ninguna entrada, así que el resultado tiene
   dominio $R(r)$.
2. **La consulta `maximas(region)` es segura.** Por el Teorema 6,
   `magnitudMaxima(r)` tiene el mismo dominio $R(r)$. Como $x \in R(r)$,
   `maximas(x)` está definido, no lanza `NoSuchElementException` y vale
   $M(r)(x)$.
3. **Los tres campos.** Para cada $x \in R(r)$, el primer campo es
   $n = |S(x, r)|$ (paso 1), el segundo es $M(r)(x)$ (paso 2) y el tercero es
   `replicasSensibles(M(r)(x), minimo).length`. Por el Lema 5 esa llamada
   termina y, por el Teorema del §4.2, devuelve $RS(M(r)(x), c)$, así que su
   longitud es $|RS(M(r)(x), c)|$.
4. **Conclusión.** Cada llave $x \in R(r)$ tiene asociado
   `Resumen`$(|S(x,r)|,\ M(r)(x),\ |RS(M(r)(x), c)|)$, que es $I(r, c)(x)$. $\blacksquare$

### Propiedades

* **Registro vacío:** $`R([\,]) = \emptyset`$, así que `informe(List(), c)` es
  `Map()`.
* **Las mismas regiones del registro:**
  $\text{dom}(\texttt{informe}(r, c)) = R(r)$, igual que `conteoPorRegion`.
* **Los conteos suman el largo del registro:** por el Lema 4,
  $`\sum_{x \in R(r)} |S(x, r)| = |r|`$, es decir, la suma de los campos `sismos`
  de todos los resúmenes es $|r|$.
* **Las réplicas no crecen al subir el mínimo:** si $c \leq c'$, entonces
  $RS(\mu, c')$ es un prefijo de $RS(\mu, c)$, porque $m_k > c' \Rightarrow m_k > c$.
  Por eso la tercera componente no aumenta cuando aumenta el mínimo, mientras
  que las dos primeras no dependen de él.
* **Mínimo inalcanzable:** si $c \geq M(r)(x)$, entonces $m_1 \leq c$ y
  $`RS(M(r)(x), c) = [\,]`$, con lo que el tercer campo es 0.

### Independencia del orden de recorrido

**Teorema 9.** Si $r'$ es un reordenamiento de $r$, entonces
$\texttt{informe}(r', c) = \texttt{informe}(r, c)$.

*Demostración.* Por el Teorema 7, $R(r') = R(r)$,
$C(r') = C(r)$ y $M(r') = M(r)$. Por el Teorema 8, cada componente del
`Resumen` de una región $x$ es función solo de $C(r)(x)$, de $M(r)(x)$ y
de $c$, y esos tres valores son iguales para $r$ y $r'$. Los dos mapas tienen
entonces el mismo dominio y el mismo valor en cada llave. $\blacksquare$

### Comprobación con pruebas

Las pruebas de `RegistroSismicoTestExtras` del Punto 5 verifican, sobre
registros concretos, lo que se demostró aquí: `informe: tiene exactamente las
regiones del registro`, `informe: la suma de los sismos es el largo del
registro`, `informe: no depende del orden del registro` (Teorema 9) y
`informe: con un mínimo inalcanzable todas las regiones tienen cero réplicas`.