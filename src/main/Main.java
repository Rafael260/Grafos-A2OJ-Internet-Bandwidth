package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

import javax.swing.plaf.synth.SynthSeparatorUI;


public class Main {

	public class Vertice {
		
		public static final int BRANCO = 0;
		public static final int CINZA = 1;
		public static final int PRETO = 2;
		
		private int valor;
		private List<Vertice> adjacentes;

		private Vertice pai;
		private int cor;

		public Vertice(int valor) {
			this.valor = valor;
			this.adjacentes = new LinkedList<>();
			this.cor = BRANCO;
		}

		public int getValor() {
			return valor;
		}

		public List<Vertice> getAdjacentes() {
			return adjacentes;
		}

		public void adicionarAresta(Vertice vertice) {
			if(!this.adjacentes.contains(vertice)) {
				this.adjacentes.add(vertice);
			}
		}
		
		public void removerAresta(Vertice vertice) {
			this.adjacentes.remove(vertice);
		}

		public Vertice getPai() {
			return pai;
		}

		public void setPai(Vertice pai) {
			this.pai = pai;
		}
		
		public int getCor() {
			return this.cor;
		}

		public void setCor(int cor) {
			this.cor = cor;
		}

	}

	public class Grafo {
		private List<Vertice> vertices;
		private int verticeInicial;
		private int[][] c;
		private int[][] f;
		
		public Grafo(int numeroVertices, int verticeInicial) {
			this.vertices = new ArrayList<>();
			this.verticeInicial = verticeInicial;
			int tamanhoMatriz = numeroVertices + verticeInicial;
			c = new int[tamanhoMatriz][tamanhoMatriz];
			f = new int[tamanhoMatriz][tamanhoMatriz];
			for (int i = verticeInicial; i < tamanhoMatriz; i++) {
				this.vertices.add(new Vertice(i));
				for (int j = verticeInicial; j < tamanhoMatriz; j++) {
					c[i][j] = 0;
					f[i][j] = 0;
				}
			}
		}

		public List<Vertice> getVertices() {
			return vertices;
		}

		public void setVertices(List<Vertice> vertices) {
			this.vertices = vertices;
		}

		public int getCapacidadeAresta(Vertice origem, Vertice destino) {
			return c[origem.getValor()][destino.getValor()];
		}

		public void inicializarVertices() {
			for (Vertice vertice : this.vertices) {
				vertice.setPai(null);
				vertice.setCor(Vertice.BRANCO);
			}
		}

		public void adicionarAresta(int origem, int destino, int capacidade) {
			Vertice verticeOrigem = this.vertices.get(origem-verticeInicial);
			Vertice verticeDestino = this.vertices.get(destino-verticeInicial);
			verticeOrigem.adicionarAresta(verticeDestino);
			verticeDestino.adicionarAresta(verticeOrigem);
			//O fluxo de banda larga pode ser passado em ambos os sentidos
			c[origem][destino] += capacidade;
			c[destino][origem] += capacidade;
		}
		
		public void removerAresta(int origem, int destino) {
			Vertice verticeOrigem = this.vertices.get(origem-verticeInicial);
			Vertice verticeDestino = this.vertices.get(destino-verticeInicial);
			verticeOrigem.removerAresta(verticeDestino);
			verticeDestino.removerAresta(verticeOrigem);
		}
		
		public boolean existeCaminhoAumentanteDFS(Vertice origem, Vertice destino) {
			inicializarVertices();
			origem.setCor(Vertice.CINZA);
			return buscarEm(origem,destino);
		}
		
		private boolean buscarEm(Vertice atual, Vertice destino) {
			if(atual == destino) {
				return true;
			}
			atual.setCor(Vertice.CINZA);
			for (Vertice adj: atual.getAdjacentes()) {
				if (adj.getCor() == Vertice.BRANCO) {
					adj.setPai(atual);
					if(buscarEm(adj,destino)) {
						return true;
					}
				}
			}
			atual.setCor(Vertice.PRETO);
			return false;
		}
		
		public boolean existeCaminhoAumentante(Vertice origem, Vertice destino) {
			inicializarVertices();
			//Lista de vertices visitados
			Queue<Vertice> filaVertices = new LinkedList<Vertice>();

			//Raiz ja foi visitada, logo fica com a cor cinza
			origem.setCor(Vertice.CINZA);
			filaVertices.add(origem);
			Vertice verticeAtual;
			List<Vertice> adjacentes;
			while(!filaVertices.isEmpty()) {
				verticeAtual = filaVertices.remove();
				adjacentes = verticeAtual.getAdjacentes();
				for (Vertice adj: adjacentes) {
					if (adj.getCor() == Vertice.BRANCO) {
						adj.setCor(Vertice.CINZA);
						adj.setPai(verticeAtual);
						//Se eu cheguei no destino, quer dizer q existe caminho aumentante
						if(adj.equals(destino)) {
							return true;
						}
						filaVertices.add(adj);
					}
				}
				verticeAtual.setCor(Vertice.PRETO);
			}
			return false;
		}
		
		public int coletarGargaloDoCaminho(Vertice origem, Vertice destino) {
			int gargalo = Integer.MAX_VALUE;
			Vertice atual = destino, anterior;
			while(true) {
				anterior = atual.getPai();
				if(c[anterior.getValor()][atual.getValor()] < gargalo) {
					gargalo = c[anterior.getValor()][atual.getValor()];
				}
				//Se chegou na origem, ja acabou de percorrer o caminho
				if(anterior == origem || anterior.equals(origem)) {
					break;
				}
				atual = anterior;
			}
			//Se existe caminho eh pq da pra passar fluxo, entao o gargalo eh maior q zero
			assert(gargalo > 0);
			return gargalo;
		}
		
		public void aumentarFluxo(Vertice origem, Vertice destino, int qtdeFluxo) {
			Vertice atual = destino, anterior;
			while(true) {
				anterior = atual.getPai();
				f[anterior.getValor()][atual.getValor()] += qtdeFluxo;
				c[anterior.getValor()][atual.getValor()] -= qtdeFluxo;
				//Arestas bidirecionais, entao a capacidade diminui para os dois lados
				c[atual.getValor()][anterior.getValor()] = c[anterior.getValor()][atual.getValor()];
				
				//Se usou o maximo da banda naquela aresta, deve ser eliminada do grafo
				if(c[anterior.getValor()][atual.getValor()] == 0) {
					//Elimina as duas por ser bidirecional
					anterior.removerAresta(atual);
					atual.removerAresta(anterior);
				}
				
				//Se chegou na origem, ja acabou de percorrer o caminho
				if(anterior.equals(origem)) {
					break;
				}
				atual = anterior;
			}
		}
		
		public int coletarFluxoMaximo(int verticeOrigem, int verticeDestino) {
			Vertice origem = this.vertices.get(verticeOrigem-verticeInicial);
			Vertice destino = this.vertices.get(verticeDestino-verticeInicial);
			int fluxo = 0;
			int gargalo;
			while(existeCaminhoAumentante(origem, destino)) {
				gargalo = coletarGargaloDoCaminho(origem, destino);
				fluxo += gargalo;
				aumentarFluxo(origem, destino, gargalo);
			}
			return fluxo;
		}
	}

	public static void main(String[] args) {
		Main mainObject = new Main();
		Scanner scanner = new Scanner(System.in);
		int numeroGrafo = 1;
		int numeroVertices;
		int origem, destino, numeroArestas;
		int verticeOrigem, verticeDestino, capacidade;
		while (true) {
			numeroVertices = scanner.nextInt();
			Grafo grafo = mainObject.new Grafo(numeroVertices, 1);
			
			if(numeroVertices == 0) {
				break;
			}
			origem = scanner.nextInt();
			destino = scanner.nextInt();
			numeroArestas = scanner.nextInt();
			
			while(numeroArestas-- > 0) {
				verticeOrigem = scanner.nextInt();
				verticeDestino = scanner.nextInt();
				capacidade = scanner.nextInt();
				grafo.adicionarAresta(verticeOrigem, verticeDestino, capacidade);
			}
			System.out.println("Network "+ numeroGrafo++);
			System.out.println("The bandwidth is "+ grafo.coletarFluxoMaximo(origem, destino)+".");
			System.out.println();
		}
	}

}
