import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import { Check } from 'lucide-react'
import { Carregando, Confirmar, Erro, Topo } from '../components/Comum'

const INTERVALO_MS = 4000

const ETAPAS = [
  { status: 'ABERTO', titulo: 'Na fila' },
  { status: 'A_CAMINHO', titulo: 'Vendedor a caminho' },
  { status: 'CONCLUIDO', titulo: 'Atendido' },
]

export default function Chamado() {
  const { id } = useParams()
  const [chamado, setChamado] = useState(null)
  const [erro, setErro] = useState('')
  const [confirmarCancelar, setConfirmarCancelar] = useState(false)

  const atualizar = useCallback(() => {
    api
      .chamado(id)
      .then((c) => {
        setChamado(c)
        setErro('')
      })
      .catch((e) => setErro(e.message))
  }, [id])

  useEffect(() => {
    atualizar()
    const timer = setInterval(atualizar, INTERVALO_MS)
    return () => clearInterval(timer)
  }, [atualizar])

  const cancelar = async () => {
    setConfirmarCancelar(false)
    try {
      setChamado(await api.cancelarChamado(id))
    } catch (e) {
      setErro(e.message)
    }
  }

  if (!chamado) {
    return (
      <div className="pagina">
        <Topo titulo="Sua senha" voltar="/" />
        {erro ? <Erro mensagem={erro} tentar={atualizar} /> : <Carregando />}
      </div>
    )
  }

  const encerrado = chamado.status === 'CONCLUIDO' || chamado.status === 'CANCELADO'
  const indiceEtapa = ETAPAS.findIndex((e) => e.status === chamado.status)

  return (
    <div className="pagina chamado">
      <Topo titulo="Sua senha" voltar={chamado.projeto ? `/projeto/${chamado.projeto.id}` : '/'} />
      <section className="senha-cartao" style={{ '--cor': chamado.corSecao }}>
        <small>Senha</small>
        <strong className="senha">{String(chamado.senha).padStart(3, '0')}</strong>
        <span>
          Corredor {chamado.corredor} · {chamado.secao}
        </span>
      </section>

      {chamado.status === 'CANCELADO' ? (
        <p className="status-texto">Chamado cancelado.</p>
      ) : (
        <ol className="etapas">
          {ETAPAS.map((etapa, i) => (
            <li key={etapa.status} className={i < indiceEtapa ? 'feita' : i === indiceEtapa ? 'atual' : ''}>
              <span className="etapa-bolinha">{i < indiceEtapa || (i === indiceEtapa && encerrado) ? <Check size={14} strokeWidth={3} /> : i + 1}</span>
              {etapa.titulo}
            </li>
          ))}
        </ol>
      )}

      <section className="status-detalhe">
        {chamado.status === 'ABERTO' && (
          <>
            <h2>{chamado.posicaoNaFila <= 1 ? 'Você é o próximo' : `${chamado.posicaoNaFila - 1} pessoa(s) na sua frente`}</h2>
            <p>Esperando há {chamado.minutosEsperando < 1 ? 'menos de 1' : chamado.minutosEsperando} min. Pode continuar comprando: esta tela se atualiza sozinha.</p>
          </>
        )}
        {chamado.status === 'A_CAMINHO' && (
          <>
            <h2>{chamado.vendedor} está indo até você</h2>
            <p>Fique perto do corredor {chamado.corredor}.</p>
          </>
        )}
        {chamado.status === 'CONCLUIDO' && (
          <>
            <h2>Atendimento concluído</h2>
            <p>Obrigado por comprar na Leroy Merlin!</p>
          </>
        )}
        <div className="motivo">
          <small>Seu pedido</small>
          <p>{chamado.motivo}</p>
          {chamado.projeto && (
            <p className="dica">
              O vendedor já vê a sua lista “{chamado.projeto.titulo}” ({chamado.projeto.totalItens} itens), então você não precisa
              explicar tudo de novo.
            </p>
          )}
        </div>
      </section>

      <Erro mensagem={erro} />

      <div className="botoes-lado fixo">
        {chamado.projeto ? (
          <Link className="botao secundario" to={`/projeto/${chamado.projeto.id}`}>
            Voltar à minha lista
          </Link>
        ) : (
          <Link className="botao secundario" to="/">
            Início
          </Link>
        )}
        {!encerrado && (
          <button className="botao perigo" onClick={() => setConfirmarCancelar(true)}>
            Cancelar chamado
          </button>
        )}
      </div>

      <Confirmar
        aberto={confirmarCancelar}
        titulo="Cancelar o chamado?"
        texto="Você sai da fila e perde a sua senha."
        confirmar="Cancelar chamado"
        cancelar="Continuar na fila"
        perigo
        aoCancelar={() => setConfirmarCancelar(false)}
        aoConfirmar={cancelar}
      />
    </div>
  )
}
