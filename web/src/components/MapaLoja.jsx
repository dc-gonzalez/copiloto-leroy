const LARGURA_PRATELEIRA = 14
const TOPO = 105
const BASE = 495

function caminhoSvg(pontos) {
  return pontos.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x} ${p.y}`).join(' ')
}

/**
 * Top-down store plan. Each aisle is flanked by two shelves in its section colour;
 * the route is drawn over the walkway graph returned by the API.
 */
export default function MapaLoja({ mapa, rota, paradaAtual, concluidas = [], destacarCorredor }) {
  if (!mapa) {
    return <div className="mapa-vazio">Carregando mapa da loja…</div>
  }
  const especial = (tipo) => mapa.nos.find((n) => n.tipo === tipo)
  const entrada = especial('ENTRADA')
  const caixa = especial('CAIXA')
  const atendimento = especial('ATENDIMENTO')
  const corredoresNaRota = new Set((rota?.paradas || []).map((p) => p.corredor))

  return (
    <svg className="mapa" viewBox="10 20 980 630" role="img" aria-label="Mapa da loja com a sua rota">
      <rect x="22" y="30" width="956" height="610" rx="18" className="mapa-piso" />

      {mapa.corredores.map((c) => {
        const ativo = corredoresNaRota.has(c.numero) || destacarCorredor === c.numero
        const esmaecer = (rota || destacarCorredor) && !ativo
        return (
          <g key={c.numero} opacity={esmaecer ? 0.35 : 1}>
            <rect x={c.x - 37} y={TOPO} width={LARGURA_PRATELEIRA} height={BASE - TOPO} rx="3" fill={c.cor} />
            <rect x={c.x + 23} y={TOPO} width={LARGURA_PRATELEIRA} height={BASE - TOPO} rx="3" fill={c.cor} />
            <text
              x={c.x - 30}
              y={(TOPO + BASE) / 2}
              className="mapa-secao"
              transform={`rotate(-90 ${c.x - 30} ${(TOPO + BASE) / 2})`}
            >
              {c.secao}
            </text>
            <circle cx={c.x} cy="58" r="17" fill={ativo ? c.cor : '#fff'} stroke={c.cor} strokeWidth="3" />
            <text x={c.x} y="64" className={ativo ? 'mapa-numero ativo' : 'mapa-numero'} fill={ativo ? '#fff' : c.cor}>
              {c.numero}
            </text>
          </g>
        )
      })}

      {rota && (
        <>
          <path d={caminhoSvg(rota.caminho)} className="mapa-rota-sombra" />
          <path d={caminhoSvg(rota.caminho)} className="mapa-rota" />
        </>
      )}

      <g className="mapa-ponto">
        <rect x={entrada.x - 50} y={entrada.y - 4} width="100" height="30" rx="6" fill="#2b2b2b" />
        <text x={entrada.x} y={entrada.y + 16}>ENTRADA</text>
      </g>
      <g className="mapa-ponto">
        <rect x={caixa.x - 40} y={caixa.y - 4} width="80" height="30" rx="6" fill="#2b2b2b" />
        <text x={caixa.x} y={caixa.y + 16}>CAIXAS</text>
      </g>
      <g className="mapa-ponto">
        <rect x={atendimento.x - 62} y={atendimento.y - 4} width="124" height="30" rx="6" fill="#2f7d12" />
        <text x={atendimento.x} y={atendimento.y + 16}>ATENDIMENTO</text>
      </g>

      {rota?.paradas.map((p) => {
        const feita = concluidas.includes(p.corredor)
        const atual = paradaAtual === p.corredor
        return (
          <g key={p.corredor} className={atual ? 'mapa-parada atual' : 'mapa-parada'}>
            {atual && <circle cx={p.x} cy={p.y} r="30" fill={p.cor} opacity="0.3" className="pulso" />}
            <circle cx={p.x} cy={p.y} r="20" fill={feita ? '#2f7d12' : '#fff'} stroke={feita ? '#2f7d12' : p.cor} strokeWidth="5" />
            {feita ? (
              <path d={`M${p.x - 8} ${p.y} l5.5 5.5 l10.5 -11`} fill="none" stroke="#fff" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
            ) : (
              <text x={p.x} y={p.y + 7} fill="#222">
                {p.ordem}
              </text>
            )}
          </g>
        )
      })}
    </svg>
  )
}
