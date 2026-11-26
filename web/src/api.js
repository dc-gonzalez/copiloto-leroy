const BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(status, mensagem, corpo) {
    super(mensagem)
    this.status = status
    this.corpo = corpo
  }
}

async function chamar(caminho, { metodo = 'GET', corpo } = {}) {
  let resposta
  try {
    resposta = await fetch(BASE + caminho, {
      method: metodo,
      headers: corpo !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: corpo !== undefined ? JSON.stringify(corpo) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Não foi possível falar com o servidor. Verifique sua conexão.')
  }
  const texto = await resposta.text()
  const dados = texto ? JSON.parse(texto) : null
  if (!resposta.ok) {
    throw new ApiError(resposta.status, dados?.mensagem || 'Algo deu errado. Tente novamente.', dados)
  }
  return dados
}

export const api = {
  saude: () => chamar('/'),
  mapa: () => chamar('/mapa'),
  buscarProdutos: (busca, limite = 20) =>
    chamar(`/produtos?busca=${encodeURIComponent(busca)}&limite=${limite}`),
  produto: (sku) => chamar(`/produtos/${sku}`),
  localizar: (sku) => chamar(`/produtos/${sku}/localizacao`),
  reporGondola: (sku) => chamar(`/produtos/${sku}/reposicao`, { metodo: 'PUT', corpo: {} }),
  conversar: (mensagens, imagemBase64) =>
    chamar('/assistente/conversa', { metodo: 'POST', corpo: { mensagens, imagemBase64 } }),
  criarProjeto: (projeto) => chamar('/projetos', { metodo: 'POST', corpo: projeto }),
  projeto: (id) => chamar(`/projetos/${id}`),
  rota: (id) => chamar(`/projetos/${id}/rota`),
  registrarRuptura: (sku, projetoId) =>
    chamar('/rupturas', { metodo: 'POST', corpo: { sku, projetoId } }),
  desfechoRuptura: (id, desfecho) =>
    chamar(`/rupturas/${id}/desfecho`, { metodo: 'PUT', corpo: { desfecho } }),
  abrirChamado: (chamado) => chamar('/chamados', { metodo: 'POST', corpo: chamado }),
  chamado: (id) => chamar(`/chamados/${id}`),
  fila: () => chamar('/chamados'),
  aceitarChamado: (id, vendedor) =>
    chamar(`/chamados/${id}/aceite`, { metodo: 'PUT', corpo: { vendedor } }),
  concluirChamado: (id) => chamar(`/chamados/${id}/conclusao`, { metodo: 'PUT', corpo: {} }),
  cancelarChamado: (id) => chamar(`/chamados/${id}/cancelamento`, { metodo: 'PUT', corpo: {} }),
  indicadores: () => chamar('/indicadores'),
}

/** "1 item", "3 itens": count followed by the right Portuguese form. */
export const plural = (quantidade, singular, pluralForma) => `${quantidade} ${quantidade === 1 ? singular : pluralForma}`

export const reais = (valor) =>
  Number(valor || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

/** Shrinks a photo to at most 1024px so the upload stays small on mobile data. */
export function reduzirImagem(arquivo, lado = 1024) {
  return new Promise((resolve, reject) => {
    const leitor = new FileReader()
    leitor.onerror = reject
    leitor.onload = () => {
      const img = new Image()
      img.onerror = reject
      img.onload = () => {
        const escala = Math.min(1, lado / Math.max(img.width, img.height))
        const canvas = document.createElement('canvas')
        canvas.width = Math.round(img.width * escala)
        canvas.height = Math.round(img.height * escala)
        canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height)
        resolve(canvas.toDataURL('image/jpeg', 0.8))
      }
      img.src = leitor.result
    }
    leitor.readAsDataURL(arquivo)
  })
}
