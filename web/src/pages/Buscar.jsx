import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { Navigation, Search } from 'lucide-react'
import { Carregando, Erro, Localizacao, Modal, ProdutoLinha, Topo, useMapa } from '../components/Comum'
import MapaLoja from '../components/MapaLoja'
import Ruptura from '../components/Ruptura'

const SUGESTOES = ['bucha', 'tinta branca', 'lâmpada led', 'torneira', 'veda rosca', 'porcelanato']

export default function Buscar() {
  const [parametros, setParametros] = useSearchParams()
  const termo = parametros.get('q') || ''
  const [texto, setTexto] = useState(termo)
  const [resultados, setResultados] = useState(null)
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState('')
  const [localizacao, setLocalizacao] = useState(null)
  const [alternativas, setAlternativas] = useState(null)
  const [localizando, setLocalizando] = useState('')
  const mapa = useMapa()

  useEffect(() => {
    if (!termo.trim()) {
      setResultados(null)
      return
    }
    let ativo = true
    setCarregando(true)
    setErro('')
    api
      .buscarProdutos(termo)
      .then((r) => ativo && setResultados(r))
      .catch((e) => ativo && setErro(e.message))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [termo])

  const pesquisar = (valor) => {
    setTexto(valor)
    setParametros(valor.trim() ? { q: valor.trim() } : {})
  }

  const levar = async (produto) => {
    setLocalizando(produto.sku)
    setErro('')
    try {
      setLocalizacao(await api.localizar(produto.sku))
    } catch (e) {
      if (e.status === 409) setAlternativas(e.corpo)
      else setErro(e.message)
    } finally {
      setLocalizando('')
    }
  }

  return (
    <div className="pagina buscar">
      <Topo titulo="Encontrar produto" voltar="/" />
      <form
        className="campo-busca barra-busca"
        onSubmit={(e) => {
          e.preventDefault()
          pesquisar(texto)
        }}
      >
        <Search size={20} className="campo-icone" aria-hidden="true" />
        <input
          autoFocus
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          placeholder="Nome, marca ou código (LM…)"
          aria-label="Buscar produto"
        />
        <button className="botao" type="submit">
          Buscar
        </button>
      </form>

      {!termo && (
        <div className="respostas-rapidas">
          {SUGESTOES.map((s) => (
            <button key={s} className="chip" onClick={() => pesquisar(s)}>
              {s}
            </button>
          ))}
        </div>
      )}

      <Erro mensagem={erro} />
      {carregando && <Carregando texto="Procurando…" />}
      {resultados && !carregando && resultados.length === 0 && (
        <p className="vazio">Não achei “{termo}”. Tente outra palavra ou fale com o assistente.</p>
      )}
      <div className="resultados">
        {resultados?.map((p) => (
          <ProdutoLinha
            key={p.sku}
            produto={p}
            extra={
              <>
                <Localizacao produto={p} />
                {p.emRuptura && <span className="tag tag-aviso">Pode ter acabado na prateleira</span>}
                {!p.podeLevarAgora && !p.emRuptura && p.disponibilidade !== 'MOSTRUARIO' && (
                  <span className="tag tag-aviso">Sem estoque na loja</span>
                )}
              </>
            }
          >
            <button className="botao pequeno" onClick={() => levar(p)} disabled={localizando === p.sku}>
              <Navigation size={16} />
              {localizando === p.sku ? 'Localizando…' : 'Me leve até lá'}
            </button>
          </ProdutoLinha>
        ))}
      </div>

      <Modal titulo="Como chegar" aberto={!!localizacao} aoFechar={() => setLocalizacao(null)}>
        {localizacao && (
          <>
            <MapaLoja mapa={mapa} rota={localizacao.rota} paradaAtual={localizacao.produto.corredor} />
            <div className="destino">
              <strong>
                Corredor {localizacao.produto.corredor} · Prateleira {localizacao.produto.prateleira}
              </strong>
              <span>{localizacao.produto.orientacao}</span>
              <small>
                {Math.round(localizacao.rota.paradas[0]?.distanciaDesdeAnteriorM || 0)} m da entrada · cerca de{' '}
                {Math.max(1, Math.round((localizacao.rota.paradas[0]?.distanciaDesdeAnteriorM || 0) / 50))} min a pé
              </small>
            </div>
            <ProdutoLinha produto={localizacao.produto} />
          </>
        )}
      </Modal>

      {alternativas && (
        <Ruptura
          alternativas={alternativas}
          aoFechar={() => setAlternativas(null)}
          aoEscolherSimilar={(similar) => {
            setAlternativas(null)
            levar(similar)
          }}
        />
      )}
    </div>
  )
}
