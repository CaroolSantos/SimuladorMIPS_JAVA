
import java.io.IOException;

import jline.console.ConsoleReader;
import br.ufrpe.deinfo.aoc.mips.MIPS;
import br.ufrpe.deinfo.aoc.mips.Simulator;
import br.ufrpe.deinfo.aoc.mips.State;


public class MyMIPS implements MIPS {
	
	@SuppressWarnings("unused")
	private ConsoleReader console;
	
	public MyMIPS() throws IOException {
		this.console = Simulator.getConsole();
	}
	
	@Override
	public void execute(State state) throws Exception {
		
		Integer PC = state.getPC();
		Integer instructionCode = state.readInstructionMemory(PC);
		
		Simulator.debug("Instruction Code = " + instructionCode);
		
		//Opcode
		//Integer opCode = ((instructionCode.intValue()&(63<<26))>>26);
		Integer opCode = ((instructionCode >> 26)&63);
		Simulator.debug("Opcode: = " + opCode);		//Funcionou até aqui 28/04
		
		Integer rs = ((instructionCode.intValue()&(31<<21))>>21);
		Integer rt = ((instructionCode.intValue()&(31<<16))>>16);
		Integer rd = ((instructionCode.intValue()&(31<<11))>>11);
		Integer imediate = ((instructionCode.intValue() & 65535));
		Integer funct = ((instructionCode.intValue()& 63));
		Integer shamt = ((instructionCode.intValue() & (31<<6))>>6);
		Integer sigExtIm = signExtend(imediate);
		Integer adress = instructionCode&67108863;
		//Integer jAdress = ((instructionCode&67108863)<<2);
		//Integer zero = 0;
		
		Integer vrs = state.readRegister(rs);
		Integer vrt = state.readRegister(rt);
		long uvrs = vrs&0xffffffffL;
		long uvrt = vrt&0xffffffffL;
		
		switch (opCode) {
		case 0:									//opcode = 000000
			Simulator.debug("RS: = " + rs);
			Simulator.debug("RT: = " + rt);
			Simulator.debug("RD: = " + rd);
			//Integer vrs = state.readRegister(rs);
			//Integer vrt = state.readRegister(rt);
			switch (funct){
			case 0:		//funct = 0x00			sll $rd, $rt, shamt
				Simulator.debug("RT = " + vrt);
				Simulator.debug("Shamt = " + shamt);
				state.writeRegister(rd, vrt<<shamt);
				Simulator.debug("Valor de RD = " + state.readRegister(rd));
				break;
			
			case 2:		//funct = 0x02			srl $rd, $rt, shamt
				Simulator.debug("RT = " + vrt);
				Simulator.debug("Shamt = " + shamt);
				state.writeRegister(rd, vrt>>shamt);
				Simulator.debug("Valor de RD = " + state.readRegister(rd));
				break;
			case 8:		//funct = 0x08			//jr $ra - $pc = $rs
				state.setPC(vrs - 4);
				Simulator.debug("PC = " + state.getPC());
				Simulator.debug("RS = " + vrs);
				Simulator.debug("jr");
				break;
			
			case 32:	//funct = 0x20			//add $rd, $rs, $rt
				
				long overSum = ((vrs + vrt)>>32);		
				//int result = (int)(overSum|0)>>32;
				int result = (int)overSum;
				state.writeRegister(rd, result);
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				Simulator.debug("add");
				break;
				
			case 33:	//funct 0x21			//addu $rd, $rs, $rt
				state.writeRegister(rd, ((int)(uvrs + uvrt)));
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				Simulator.debug("addu");				
				break;
			
			case 34:	//funct = 0x22			//sub $rd, $rs, $rt
				long overSub = ((vrs - vrt))>>32;
				state.writeRegister(rd, (int)overSub);
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				Simulator.debug("sub");
				break;
			
			case 35:	//funct = 0x23			//subu $rd, $rs, $rt
				state.writeRegister(rd, ((int)(uvrs - uvrt)));
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				Simulator.debug("subu");
				break;
			case 36:	//funct = 0x24			//and $rd, $rs, $rt
				state.writeRegister(rd, (vrs&vrt));
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				Simulator.debug("and");
				break;
				
			case 37:	//funct = 0x25			//or $rd, $rs, $rt
				Simulator.debug("Valor de rs = " + vrs);
				Simulator.debug("Valor de rt = " + vrt);
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				state.writeRegister(rd, (vrs|vrt));
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				Simulator.debug("or");
				break;
			case 39:	//funct = 0x27			//nor $rd, $rs, $rt
				state.writeRegister(rd, ~(vrs|vrt));
				Simulator.debug("Valor de rd = " + state.readRegister(rd));
				Simulator.debug("nor");
				break;
			case 42:	//funct = 0x2a			//slt $rd, $rs, $rt
				Simulator.debug("Valor de RS = " + vrs);
				Simulator.debug("Valor de RT = " + vrt);
				state.writeRegister(rd, ((vrs<vrt)?1:0));
				Simulator.debug("Valor de RD = " + state.readRegister(rd));
				Simulator.debug("slt");
				break;
				
			case 43:	//funct = 0x2b			//sltu $rd, $rs, $rt
				if(Integer.compareUnsigned(vrs, vrt) < 0){
					state.writeRegister(rd, 1);		//R[rs] < R[vrt]
				}
				else{
					state.writeRegister(rd, 0);		//R[rs] > R[vrt]
				}
				Simulator.debug("Valor de RD: = " + rd);
				Simulator.debug("sltu");
				break;
			default:
				break;
			}
			state.setPC(state.getPC() + 4);
			break;
		
		case 2:										//j jumpAdress
			Integer jAdress = (adress<<2)|(state.getPC()&(1111<<28));
			Simulator.debug("jAdress = " + jAdress);
			state.setPC(jAdress);
			Simulator.debug("PC = " + state.getPC());
			Simulator.debug("j");
			break;
		
		case 3:										//jal jAdress
			Integer jalAdress = (adress<<2)|(state.getPC()&(1111<<28));
			Simulator.debug("jalAdress = " + jalAdress);
			state.writeRegister(31, state.getPC()+4);
			state.setPC(jalAdress);
			Simulator.debug("PC after Jal = " + state.getPC());
			Simulator.debug("jal");
			break;
		case 4:									//beq $rs, $rt, label
			if((vrs-vrt) == 0){
				Integer ba = branchAdress(imediate);
				Simulator.debug("Branc adress = " + (ba + state.getPC() + 4));
				state.setPC(ba + state.getPC() + 4);
			}
			else{
				state.setPC(state.getPC() + 4);
			}
			Simulator.debug("beq");
			break;
		case 5:									//bne $rs, $rt, label
			if((vrs - vrt) != 0){
				Integer ba = branchAdress(imediate);
				Simulator.debug("Branch adress = " + (ba + state.getPC() + 4));
				state.setPC(ba + state.getPC() + 4);
			}
			else{
				state.setPC(state.getPC() + 4);
			}
			Simulator.debug("bne !=");
			break;
		case 8:									//addi $rt, $rs, imediate
			Simulator.debug("RS: = " + rs);
			Simulator.debug("RT: = " + rt);
			
			//int imSe = signExtend(imediate);
			//vrs = signExtend(vrs);
			
			//state.writeRegister(rt, Integer.sum(vrs, (imSe)));
			long overI = ((vrs + sigExtIm)>>32);
			state.writeRegister(rt, (int)(overI)); 		//novo
			Simulator.debug("Imse: " + sigExtIm);
			Simulator.debug("Imediate" + imediate);
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			Simulator.debug("addi");
			
			state.setPC(state.getPC() + 4);
			break;
		case 9:									//addiu $rt, $rs, sigExImediate
			//long lvrs = vrs&0xffffffffL;
			state.writeRegister(rt, ((int)(uvrs + sigExtIm)));
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			state.setPC(state.getPC() + 4);
			break;
		case 10:								//slti $rt, $rs, sigExImediate
			Simulator.debug("Valor de RS = " + vrs);
			Simulator.debug("imSe = " + sigExtIm);
			state.writeRegister(rt, ((vrs<sigExtIm)?1:0));
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			Simulator.debug("slti");
			
			state.setPC(state.getPC() + 4);
			break;
			
		case 11:								//sltiu $rt, $rs, sigExImediate
			//long lvrs = vrs&0xffffffffL;
			if(Integer.compareUnsigned(vrs, sigExtIm) < 0){
				state.writeRegister(rt, 1);		//R[rs] < sigExIm
			}
			else{
				state.writeRegister(rt, 0);		//R[rs] > sigExIm
			}
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			Simulator.debug("sltiu");
			state.setPC(state.getPC() + 4);
			break;
		case 12:								//andi $rt, $rs, sigExImediate
			Simulator.debug("RS: = " + rs);
			Simulator.debug("RT: = " + rt);
			
			state.writeRegister(rt, (vrs&(imediate & 0b11111111111111111111111111111111)));
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			Simulator.debug("andi");
			
			state.setPC(state.getPC() + 4);
			
			break;
	
		case 13:								//ori $rt, $rs, imediate
			Simulator.debug("RS: = " + rs);
			Simulator.debug("RT: = " + rt);
			
			state.writeRegister(rt, (vrs|(imediate & 0b11111111111111111111111111111111)));
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			Simulator.debug("ori");
			
			state.setPC(state.getPC() + 4);
			
			break;
			
		case 15:								//lui $rt, imediate
			state.writeRegister(rt, (imediate)<<16);
			
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			Simulator.debug("lui");
			state.setPC(state.getPC() + 4);
			break;
			
		case 35:								//lw $rt, $offset(rs)
			Simulator.debug("Offset = " + sigExtIm);
			state.writeRegister(rt, state.readWordDataMemory(vrs + sigExtIm));
			Simulator.debug("Valor de RT = " + state.readRegister(rt));
			Simulator.debug("lw");
			state.setPC(state.getPC() + 4);
			break;
				
		case 36:								//lbu $rt, $offset(24b'0,$rs + offset)
			Simulator.debug("Offset = " + (sigExtIm&255));
			//Integer data = (state.readWordDataMemory(vrs + sigExtIm))&255;
			state.writeRegister(rt, (state.readWordDataMemory(vrs + sigExtIm))&255);
			Simulator.debug("Valor de rt5 = " + state.readRegister(rt));
			Simulator.debug("lbu");
			state.setPC(state.getPC() + 4);
			break;
		case 37:								//lhu
			Simulator.debug("Offset = " + (sigExtIm&65535));
			state.writeRegister(rt, (state.readWordDataMemory(vrs + sigExtIm))&65535);
			Simulator.debug("Valor de rt5 = " + state.readRegister(rt));
			Simulator.debug("lhu");
			state.setPC(state.getPC() + 4);
			break;
		case 40:								//sb $rt, $offset(rs)
			Simulator.debug("Offset = " + sigExtIm);
			Simulator.debug("End = " + ((vrs + sigExtIm)&255));
			state.writeByteDataMemory(((vrs + sigExtIm)), vrt&255);
			Simulator.debug("Valor armazenado = " + (vrt&255));
			Simulator.debug("sb");
			state.setPC(state.getPC() + 4);
			break;
		case 41:								//sh $rt, $offset([0:15]rs)
			Simulator.debug("Offset = " + sigExtIm);
			state.writeHalfwordDataMemory(((vrs + sigExtIm)), vrt&65535);
			//state.writeHalfwordDataMemory((vrs + sigExtIm)<<16, vrt<<16);
			Simulator.debug("Valor armazenado = " + (vrt&65535));
			Simulator.debug("sh");
			state.setPC(state.getPC() + 4);
			break;
		case 43:								//sw $rt, offset(rs)
			Simulator.debug("Offset = " + sigExtIm);
			state.writeWordDataMemory(vrs + sigExtIm, vrt);
			Simulator.debug("Valor armazenado = " + state.readWordDataMemory(vrs+ sigExtIm));
			Simulator.debug("sw");
			state.setPC(state.getPC() + 4);
			break;
		default:
			state.setPC(state.getPC() + 4);
			break;
		}
		//state.setPC(state.getPC() + 4);
	}

	public static void main(String[] args) {
		try {
			Simulator.setMIPS(new MyMIPS());
			//Simulator.setLogLevel(Simulator.LogLevel.INFO);
			Simulator.setLogLevel(Simulator.LogLevel.DEBUG);
			Simulator.start();
		} catch (Exception e) {		
			e.printStackTrace();
		}		
	}
	
	static int signExtend(Integer val){
		if(((val&32768)>>15) == 1){
			return val|(65535<<16);
		}
		else{
			return val|(0b00000000000000000000000000000000);
		}
	}
	
	static int branchAdress(int im){
		if(((im&32768)>>15) == 1){
			return (im|(16383<<16))<<2;
		}
		else{
			return ((16383)&im)<<2;
		}
		//return im;
	}
}


