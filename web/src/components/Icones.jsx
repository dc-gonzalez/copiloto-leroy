import {
  BrickWall, Bolt, CircleHelp, Droplet, Hammer, Headset, Layers, LayoutGrid, Lightbulb, Package,
  PaintRoller, ShowerHead, Sofa, Sprout, Zap,
} from 'lucide-react'

const POR_SECAO = {
  Jardim: Sprout,
  Decoração: Sofa,
  Iluminação: Lightbulb,
  Elétrica: Zap,
  Hidráulica: Droplet,
  'Banheiro e Metais': ShowerHead,
  'Madeiras e Organização': Layers,
  'Fixação e Ferragens': Bolt,
  Ferramentas: Hammer,
  Tintas: PaintRoller,
  'Pisos e Revestimentos': LayoutGrid,
  'Materiais Básicos': BrickWall,
}

export function IconeSecao({ secao, cor, tamanho = 20 }) {
  const Icone = POR_SECAO[secao] || Package
  return <Icone size={tamanho} color={cor} strokeWidth={1.75} aria-hidden="true" />
}

/** Tile with the section icon over a light tint of the section colour. */
export function SeloSecao({ secao, cor, tamanho = 44 }) {
  return (
    <span className="selo-secao" style={{ width: tamanho, height: tamanho, background: `${cor}1f`, color: cor }}>
      <IconeSecao secao={secao} cor={cor} tamanho={Math.round(tamanho * 0.48)} />
    </span>
  )
}

export const ICONE_ATENDIMENTO = {
  APOIO_PARCIAL: CircleHelp,
  DEPENDE_VENDEDOR: Headset,
}
