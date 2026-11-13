# Informe de corrección

Este informe argumenta que las funciones de los Puntos 1 a 5 hacen lo que pide
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
En `paresCercanos` la comparación lleva además una tolerancia
$\varepsilon = 10^{-9}$ (ver el Teorema 3).

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
sin repetir, en el orden de su primera aparición. Sea $\varepsilon = 10^{-9}$ la
constante `tolerancia` del código y

```math
C(a, b, r, d) \equiv \exists s_1, s_2 \in r : s_1\text{.region} = a \land s_2\text{.region} = b \land |s_1\text{.magnitud} - s_2\text{.magnitud}| \leq d + \varepsilon
```

la condición "algún sismo de $a$ y algún sismo de $b$ difieren a lo sumo $d$,
con la tolerancia $\varepsilon$". La tolerancia es una decisión de diseño: con
`Double`, `3.1 - 3.0` da `0.10000000000000009` y la comparación exacta
$\leq 0.1$ rechazaría una pareja cuya diferencia decimal es exactamente $0.1$.
La condición del enunciado y $C$ solo difieren cuando la diferencia real cae en
$(d,\ d + \varepsilon]$, algo que no ocurre con magnitudes de pocos decimales.
La función que describe el enunciado, con esta condición, es

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

**Paso 2: `cercanas(a, b)` es verdadera si y solo si $C(a, b, r, d)$.** (En el
código, `d + tolerancia` es $d + \varepsilon$.) Su
traducción es

```math
\text{flatMap}\big(s_1 \mapsto \text{map}(s_2 \mapsto (s_1, s_2),\ \text{filter}(q_{s_1}, r)),\ r\big)
```

con $q_{s_1}(s_2) \equiv s_1\text{.region} = a \land s_2\text{.region} = b \land |s_1\text{.magnitud} - s_2\text{.magnitud}| \leq d + \varepsilon$.
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

### Resultado independiente del orden de recorrido (Punto 1)

Sea $r'$ un reordenamiento de $r$ (ver Convenciones). Para cada valor $v$, sea $\text{veces}_v(L)$ el número de apariciones de
$v$ en la lista $L$.

#### Lema 1b: contar apariciones en un filtro y una proyección

Con $L(r) = \text{map}(g, \text{filter}(p, r))$ como en el Lema 1:

```math
\text{veces}_v(L(r)) = \sum_{s \in r} [\,p(s) \land g(s) = v\,]
```

*Demostración.* Por inducción sobre $r$. Para $`r = [\,]`$, $`L(r) = [\,]`$ y la suma
vacía vale 0. Para $r = s :: r'$, por el Lema 1, $L(s :: r')$ es $g(s) :: L(r')$
si $p(s)$ y $L(r')$ si no. En el primer caso $v$ aparece una vez más exactamente
cuando $g(s) = v$, y en el segundo caso no se añade nada; en ambos, el aporte de
$s$ es $`[\,p(s) \land g(s) = v\,]`$, y el resto es $\text{veces}_v(L(r'))$, que por
la HI es la suma sobre $r'$. $\blacksquare$

#### Teorema 3b: `fuertes` y `enRegion`

Para todo $u$ y toda región $x$, $\texttt{fuertes}(r', u)$ es un reordenamiento de
$\texttt{fuertes}(r, u)$, y $\texttt{enRegion}(r', x)$ lo es de
$\texttt{enRegion}(r, x)$.

*Demostración.* Por los Teoremas 1 y 2, las dos funciones son de la forma
$L(r)$ del Lema 1, con $p$ y $g$ que dependen solo del sismo $s$ y no de su
posición ni de los demás sismos. Por el Lema 1b, $\text{veces}_v(L(r))$ es una
suma con un sumando por sismo, y cada sumando depende solo de ese sismo. La suma
de enteros es conmutativa y asociativa, y $r'$ tiene los mismos sismos con las
mismas repeticiones, así que $\text{veces}_v(L(r')) = \text{veces}_v(L(r))$ para
todo $v$. Dos listas con las mismas apariciones de cada valor son
reordenamientos una de la otra. $\blacksquare$

El orden dentro de la lista sí cambia, y el enunciado lo pide así: por el Lema 1,
cada sismo que cumple $p$ antepone su valor en el orden en que aparece en el
registro. Por eso las listas siguen el orden del registro y, si este se invierte,
cada lista queda invertida. Lo que no cambia es **cuántas veces** sale cada
región o cada magnitud, incluidos los repetidos.

#### Teorema 3c: `paresCercanos`

Sea $`U(r, d) = \big\{\, \{a, b\} \mid a, b \in R(r),\ a \neq b,\ C(a, b, r, d) \,\big\}`$
el conjunto de parejas **no ordenadas** de regiones cercanas. Entonces:

1. $C(a, b, r', d) \iff C(a, b, r, d)$ y $C(a, b, r, d) \iff C(b, a, r, d)$.
2. $U(r', d) = U(r, d)$.
3. $\texttt{paresCercanos}(r', d)$ y $\texttt{paresCercanos}(r, d)$ tienen las
   mismas parejas no ordenadas, cada una una sola vez, y por lo tanto el mismo
   número de parejas.

*Demostración.*

* **(1)** $C$ es una condición del tipo "existen $s_1, s_2 \in r$ tales que ...":
  solo pregunta *qué* sismos hay en $r$, no en qué posición. Como $r'$ tiene los
  mismos sismos que $r$, $C(a, b, r', d) \iff C(a, b, r, d)$. Además $C$ es
  simétrica: dados $s_1$ de $a$ y $s_2$ de $b$ con
  $|s_1\text{.magnitud} - s_2\text{.magnitud}| \leq d + \varepsilon$, la pareja
  $(s_2, s_1)$ cumple la condición para $(b, a)$, porque la resta de dos `Double`
  cambia de signo sin cambiar de valor absoluto.
* **(2)** Por el Teorema 7 (1), $R(r') = R(r)$. Con (1), las dos condiciones que
  definen $U$ coinciden para $r$ y para $r'$.
* **(3)** Por el Teorema 3, $\texttt{paresCercanos}(r, d) = P(r, d)$, donde cada
  pareja $(a_i, a_j)$ con $i < j$ corresponde a una sola pareja no ordenada
  $`\{a_i, a_j\}`$ y las $a_i$ son distintas, así que cada pareja no ordenada de
  $U(r, d)$ sale exactamente una vez, en una de sus dos orientaciones. Lo mismo
  vale para $r'$, y por (2) las parejas no ordenadas son las mismas. $\blacksquare$

Lo que sí depende del orden del registro es solo la **presentación** del
resultado: el orden de la lista y cuál región de la pareja va primero, porque
ambos siguen la primera aparición de cada región ($i < j$ en el Teorema 3). Por
ejemplo, con un registro de `"Zulia"` (4.0) seguido de `"Arauca"` (4.2) el
resultado es `List(("Zulia", "Arauca"))`, y con el registro invertido sería
`List(("Arauca", "Zulia"))`: la misma pareja, orientada de otra forma. La prueba
`paresCercanos: el orden de las regiones es el de aparición, no el alfabético`
de `RegistroSismicoTestExtras` verifica esa orientación.

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

### Resultado independiente del orden de recorrido (Punto 2)

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
registro concreto: `conteoPorRegion y magnitudMaxima no dependen del orden del
registro` y `porRegion: cada grupo conserva el orden del registro`.


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
casos de $CS(r)$. Si $`r = [\,]`$, el dominio es vacío y cualquier consulta devuelve $0$. $\blacksquare$

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

```math
RA(r) =
\begin{cases}
\text{Some}\bigl(\max\nolimits_{\preceq} \text{dom}(C(r))\bigr) & \text{si } r \neq [\,] \\
\text{None} & \text{si } r = [\,]
\end{cases}
```

donde $\preceq$ es el orden total lexicográfico que compara primero por conteo
descendente y desempata por orden alfabético ascendente:

```math
a \preceq b \iff C(r)(a) > C(r)(b) \;\lor\; \bigl(C(r)(a) = C(r)(b) \land a \le_{\text{alf}} b\bigr)
```

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
$\max_{\preceq} \text{dom}(C(r))$. Si $`r = [\,]`$, la lista de tuplas es vacía y
`headOption` retorna `None`. $\blacksquare$

**Independencia del orden de recorrido.**
El resultado de $\text{regionMasActiva}(r)$ no depende del orden en que aparezcan
los sismos en $r$:

```math
\forall \pi.\; RA(\pi(r)) = RA(r)
```

Esto se debe a que $C(\pi(r)) = C(r)$ (Teorema 7) y el criterio de selección y
desempate $\preceq$ opera exclusivamente sobre el par $(\text{conteo}, \text{nombre de región})$,
sin utilizar la posición ni el índice original de los sismos en el registro. $\blacksquare$

## Punto 4 — réplicas perezosas

### 4.1 `replicas`

El código es

```scala
def replicas(magnitud: Double): LazyList[Double] = {
  def desde(actual: Double): LazyList[Double] =
    redondear(actual) #:: desde(actual * 0.8)

  desde(magnitud)
}
```

**Especificación.** Sean $a_1 = \mu$ y $a_{k+1} = 0.8 \cdot a_k$ (producto en
`Double`) la sucesión interna, sin redondear, y $`\rho(x) = \text{round}(100\,x)/100`$.
La secuencia emitida es

$$
m_k = \rho(a_k), \qquad k \ge 1
$$

El redondeo se aplica solo al emitir. No es $m_{k+1} = \rho(0.8 \cdot m_k)$,
que acumularía el error de redondeo.

**Teorema.** Para toda magnitud $a$ y todo $n \ge 1$,
`desde(a).take(n).toList` $= [\rho(a),\ \rho(0.8a),\ \rho(0.8 \cdot 0.8a),\ \ldots]$,
es decir, los primeros $n$ términos de $m_k$ para la sucesión interna que empieza
en $a$. En particular, para todo $\mu$:

$$
\text{replicas}(\mu).\text{take}(n).\text{toList} = [m_1, m_2, \ldots, m_n]
$$

**Demostración (por inducción sobre $n$, para $a$ cualquiera).**

* **Caso base ($n = 1$):** `desde(a)` es `redondear(a) #:: desde(a * 0.8)`. Su
  cabeza es $\rho(a)$ y `take(1)` retorna $[\rho(a)]$ sin evaluar la cola.
* **Paso inductivo ($n \to n+1$):** `desde(a).take(n+1)` es la cabeza $\rho(a)$
  seguida de `desde(a * 0.8).take(n)`. Por la hipótesis de inducción, aplicada
  a la magnitud $a' = 0.8 \cdot a$, esto es $[\rho(a'),\ \rho(0.8a'),\ \ldots]$,
  con $n$ términos. La sucesión interna que empieza en $a'$ es la que empieza en
  $a$ sin su primer término, así que el resultado completo es
  $[\rho(a_1), \rho(a_2), \ldots, \rho(a_{n+1})]$ con $a_1 = a$. $\blacksquare$

La función `replicas(μ)` solo llama a `desde(μ)`, de modo que el teorema vale
para ella con $a_1 = \mu$.

**Por qué no agota la memoria.**
El constructor `#::` recibe el segundo argumento por nombre (`=> LazyList[A]`).
Por lo tanto, la llamada recursiva `desde(actual * 0.8)` queda encapsulada como
una suspensión computacional. Al solicitar `take(n)`, el runtime de Scala solo
materializa $n$ celdas en memoria, memorizando cada nodo ya calculado. La
memoria empleada es proporcional a los $n$ elementos demandados ($O(n)$) y nunca
al tamaño infinito de la secuencia conceptual.

### 4.2 `replicasSensibles`

**Especificación.** $RS(\mu, c)$ es la lista de réplicas que superan
estrictamente el umbral mínimo $c$, incluyendo la primera magnitud cuando esta lo
supera:

```math
RS(\mu, c) = [m_1, \ldots, m_k], \quad k = \max \{ j \ge 0 \mid \forall i \in \{1, \ldots, j\}, m_i > c \}
```

#### Lema 5: `replicasSensibles` termina

Para toda magnitud $\mu \geq 0$ y todo $c \geq 0$, existe $K$ tal que
$m_K \leq c$. Por lo tanto `takeWhile(_ > c)` se detiene y
`replicasSensibles(μ, c)` termina. Si $\mu < 0$, entonces $m_1 \leq 0 \leq c$ y
la lista es vacía.

*Demostración.* La sucesión interna es $a_k = \mu \cdot 0.8^k$ (sin redondear)
y la que se emite es $m_k = \rho(a_k)$. Como $0 < 0.8 < 1$, $a_k \to 0$.
Entonces existe $K$ con $a_K < 0.005$, y para ese $K$ se tiene
$\rho(a_K) = 0 \leq c$. Además, $\rho$ es monótona y $a_k$ es decreciente, así que
$m_k$ es no creciente: $m_{k+1} \leq m_k$. Entonces la lista emitida es un
prefijo de elementos $> c$ seguido de elementos $\leq c$, y `takeWhile` toma
justo ese prefijo, que es finito, de longitud menor que $K$. $\blacksquare$

La demostración no usa un redondeo en cada paso: es lo que garantiza que el caso
$c = 0$ termine.

**Teorema.** $\text{replicasSensibles}(\mu, c) = RS(\mu, c)$ para todo $c \ge 0$ y
todo $\mu$.

**Demostración.** La función es `replicas(μ).takeWhile(_ > c).toList`. Por el
Teorema del §4.1, `replicas(μ)` genera sucesivamente la secuencia
$m_1, m_2, \ldots$. La función `takeWhile(p)` sobre una `LazyList` inspecciona
cada elemento secuencialmente y detiene la evaluación en el primer índice $k+1$
tal que $p(m_{k+1})$ sea falso ($m_{k+1} \le c$). Por el Lema 5 ese índice
existe, y al detenerse no se fuerza ningún elemento subsiguiente. Luego, `toList`
convierte el prefijo $[m_1, \ldots, m_k]$ en una `List` finita. Como $m_k$ es no
creciente, ese prefijo es exactamente $RS(\mu, c)$. Si $m_1 \le c$, la primera
condición falla y retorna $`[\,]`$. $\blacksquare$

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
alcanzaría nunca y `takeWhile` no terminaría (ver el Lema 5 del §4.2).

### Teorema 8: `informe`

Vamos a demostrar que $\forall r,\ \forall c \geq 0 : \texttt{informe}(r, c) == I(r, c)$.

*Demostración.* El código es

```scala
val maximas = magnitudMaxima(r)

conteoPorRegion(r).map { case (region, n) =>
  val maxima = maximas(region)
  (region, Resumen(n, maxima, replicasSensibles(maxima, minimo).length))
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
   `replicasSensibles(M(r)(x), minimo).length`. Por el Lema 5 (§4.2) esa llamada
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
