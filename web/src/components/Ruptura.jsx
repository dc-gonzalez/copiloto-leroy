import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { ChevronRight, Headset, ShoppingCart } from 'lucide-react'
import { Erro, Localizacao, Modal, ProdutoLinha } from './Comum'

/**
 * Shown when the shelf is empty although the system shows stock: offers an equivalent
 * (own brand first), buying online, or calling an associate. The choice is recorded.
 */
export default function Ruptura({ alternativas, projetoId, aoEscolherSimilar, aoDesistir, aoFechar }) {
  const navegar = useNavigate()
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  if (!alternativas) return null
  const { produto, similares, compraOnline, mensagem, rupturaId } = alternativas

  const registrar = (desfecho) => (rupturaId ? api.desfechoRuptura(rupturaId, desfecho).catch(() => {}) : Promise.resolve())

  const escolherSimilar = async (similar) => {
    await registrar('SIMILAR')
    aoEscolherSimilar?.(similar)
  }

  const comprarOnline = () => {
    registrar('COMPRA_ONLINE')
    window.open(compraOnline.url, '_blank', 'noopener')
  }

  const chamarVendedor = async () => {
    setEnviando(true)
    setErro('')
    try {
      const chamado = await api.abrirChamado({
        projetoId: projetoId || null,
        sku: produto.sku,
        corredor: produto.corredor,
        motivo: `Não encontrei na prateleira: ${produto.nome}`,
      })
      await registrar('VENDEDOR')
      navegar(`/chamado/${chamado.id}`)
    } catch (e) {
      setErro(e.message)
      setEnviando(false)
    }
  }

  const desistir = () => {
    registrar('DESISTIU')
    ;(aoDesistir || aoFechar)()
  }

  return (
    <Modal titulo="Acabou na prateleira" aberto aoFechar={aoFechar}>
      <p className="modal-texto">{mensagem}</p>
      <div className="ruptura-original">
        <ProdutoLinha produto={produto} />
      </div>

      {similares.length > 0 && (
        <>
          <h3 className="rotulo grupo">Leve um equivalente agora</h3>
          {similares.map((s) => (
            <ProdutoLinha key={s.sku} produto={s} extra={<Localizacao produto={s} />}>
              <button className="botao pequeno" onClick={() => escolherSimilar(s)}>
                Vou levar
              </button>
            </ProdutoLinha>
          ))}
        </>
      )}

      <h3 className="rotulo grupo">Ou, se preferir</h3>
      <div className="opcoes-ruptura">
        {compraOnline?.disponivel && (
          <button className="opcao" onClick={comprarOnline}>
            <span className="selo-secao neutro">
              <ShoppingCart size={20} strokeWidth={1.75} />
            </span>
            <span className="linha-acao-texto">
              <strong>Comprar no site</strong>
              <small>{compraOnline.prazo}</small>
            </span>
            <ChevronRight size={18} className="seta" />
          </button>
        )}
        <button className="opcao" onClick={chamarVendedor} disabled={enviando}>
          <span className="selo-secao neutro">
            <Headset size={20} strokeWidth={1.75} />
          </span>
          <span className="linha-acao-texto">
            <strong>{enviando ? 'Chamando…' : 'Chamar um vendedor'}</strong>
            <small>Ele confere o estoque e vem até o corredor {produto.corredor}</small>
          </span>
          <ChevronRight size={18} className="seta" />
        </button>
      </div>
      <Erro mensagem={erro} />
      <button className="link centro" onClick={desistir}>
        Deixar este item para depois
      </button>
    </Modal>
  )
}
