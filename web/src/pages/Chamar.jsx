import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Confirmar, Erro, Topo, useMapa } from '../components/Comum'
import { IconeSecao } from '../components/Icones'

const MOTIVOS = ['Tirar uma dúvida', 'Não encontro um produto', 'Preciso de um orçamento', 'Corte ou preparo de material']

export default function Chamar() {
  const navegar = useNavigate()
  const mapa = useMapa()
  const [corredor, setCorredor] = useState(null)
  const [motivo, setMotivo] = useState(MOTIVOS[0])
  const [detalhe, setDetalhe] = useState('')
  const [confirmar, setConfirmar] = useState(false)
  const [erro, setErro] = useState('')

  const abrir = async () => {
    setConfirmar(false)
    setErro('')
    try {
      const chamado = await api.abrirChamado({
        corredor,
        motivo: detalhe.trim() ? `${motivo}: ${detalhe.trim()}` : motivo,
      })
      navegar(`/chamado/${chamado.id}`)
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <div className="pagina chamar">
      <Topo titulo="Falar com um vendedor" voltar="/" />
      <section className="bloco">
        <h2>Onde você está?</h2>
        <p className="dica">Toque no corredor mais perto de você. O número fica na placa no alto.</p>
        <div className="grade-corredores">
          {mapa?.corredores.map((c) => (
            <button
              key={c.numero}
              className={corredor === c.numero ? 'corredor-botao ativo' : 'corredor-botao'}
              style={{ '--cor': c.cor }}
              onClick={() => setCorredor(c.numero)}
            >
              <span className="corredor-numero">{c.numero}</span>
              <IconeSecao secao={c.secao} cor={corredor === c.numero ? '#fff' : c.cor} tamanho={18} />
              <small>{c.secao}</small>
            </button>
          ))}
        </div>
      </section>
      <section className="bloco">
        <h2>Como podemos ajudar?</h2>
        <div className="respostas-rapidas">
          {MOTIVOS.map((m) => (
            <button key={m} className={motivo === m ? 'chip ativo' : 'chip'} onClick={() => setMotivo(m)}>
              {m}
            </button>
          ))}
        </div>
        <textarea
          value={detalhe}
          onChange={(e) => setDetalhe(e.target.value)}
          placeholder="Quer adiantar algum detalhe? (opcional)"
          maxLength={200}
          rows={3}
        />
      </section>
      <Erro mensagem={erro} />
      <div className="botoes-lado fixo">
        <button className="botao secundario" onClick={() => navegar('/assistente')}>
          Tentar o assistente
        </button>
        <button className="botao" disabled={!corredor} onClick={() => setConfirmar(true)}>
          Pegar senha
        </button>
      </div>
      <Confirmar
        aberto={confirmar}
        titulo="Chamar um vendedor?"
        texto={`Um vendedor vai até o corredor ${corredor}. Fique por perto: você acompanha a fila por aqui.`}
        confirmar="Chamar"
        aoCancelar={() => setConfirmar(false)}
        aoConfirmar={abrir}
      />
    </div>
  )
}
